package com.inkWell.comment.service;

import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndIsApprovedTrue(postId);
    }

    public Comment addComment(Comment comment) {
        comment.setApproved(true); // Auto-approve for now
        return commentRepository.save(comment);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public void softDelete(Long id) {
        Comment comment = getCommentById(id);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public List<Comment> getReplies(Long parentId) {
        return commentRepository.findByParentCommentId(parentId);
    }

    @org.springframework.transaction.annotation.Transactional
    public void likeComment(Long id) {
        commentRepository.incrementLikeCount(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void unlikeComment(Long id) {
        commentRepository.decrementLikeCount(id);
    }

    public Comment approveComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setApproved(true);
        return commentRepository.save(comment);
    }

    public Comment rejectComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setApproved(false);
        return commentRepository.save(comment);
    }
}
