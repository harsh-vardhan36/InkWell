package com.inkWell.comment.service;

import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setPostId(100L);
        testComment.setAuthorId(1L);
        testComment.setContent("Test comment");
    }

    @Test
    void getAllComments_ReturnsList() {
        when(commentRepository.findAll()).thenReturn(List.of(testComment));
        List<Comment> result = commentService.getAllComments();
        assertFalse(result.isEmpty());
    }

    @Test
    void getCommentById_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        Comment result = commentService.getCommentById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCommentById_NotFound_ThrowsException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> commentService.getCommentById(99L));
    }

    @Test
    void addComment_Success() {
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        Comment result = commentService.addComment(new Comment());
        assertTrue(result.isApproved());
        verify(commentRepository).save(any());
    }

    @Test
    void softDelete_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        commentService.softDelete(1L);
        assertTrue(testComment.isDeleted());
        verify(commentRepository).save(testComment);
    }

    @Test
    void getCommentsByPost_Success() {
        when(commentRepository.findByPostIdAndIsApprovedTrue(100L)).thenReturn(List.of(testComment));
        List<Comment> result = commentService.getCommentsByPost(100L);
        assertEquals(1, result.size());
    }

    @Test
    void likeComment_CallsRepository() {
        commentService.likeComment(1L);
        verify(commentRepository).incrementLikeCount(1L);
    }

    @Test
    void approveComment_Success() {
        testComment.setApproved(false);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);

        Comment result = commentService.approveComment(1L);

        assertTrue(result.isApproved());
        verify(commentRepository).save(testComment);
    }

    @Test
    void rejectComment_Success() {
        testComment.setApproved(true);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);

        Comment result = commentService.rejectComment(1L);

        assertFalse(result.isApproved());
        verify(commentRepository).save(testComment);
    }
}
