package com.inkWell.post.service;

import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.enums.PostStatus;
import com.inkWell.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PostService postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Title");
        testPost.setContent("Test Content with some words to calculate read time correctly.");
        testPost.setAuthorName("Test Author");
    }

    @Test
    void createPost_Success() {
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post createdPost = postService.createPost(testPost);

        assertNotNull(createdPost.getSlug());
        assertTrue(createdPost.getSlug().startsWith("test-title"));
        assertEquals(PostStatus.DRAFT, createdPost.getStatus());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void publishPost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post publishedPost = postService.publishPost(1L);

        assertEquals(PostStatus.PUBLISHED, publishedPost.getStatus());
        assertNotNull(publishedPost.getPublishedAt());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void unpublishPost_Success() {
        testPost.setStatus(PostStatus.PUBLISHED);
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post unpublishedPost = postService.unpublishPost(1L);

        assertEquals(PostStatus.DRAFT, unpublishedPost.getStatus());
    }

    @Test
    void updatePost_Success() {
        Post postDetails = new Post();
        postDetails.setTitle("Updated Title");
        postDetails.setContent("Updated Content");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post updatedPost = postService.updatePost(1L, postDetails);

        assertEquals("Updated Title", updatedPost.getTitle());
        assertTrue(updatedPost.getSlug().startsWith("updated-title"));
    }

    @Test
    void publishPost_NotFound_ThrowsException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> postService.publishPost(1L));
    }
    @Test
    void publishPost_RabbitMQFailure_StillReturnsPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("RabbitMQ down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Post result = assertDoesNotThrow(() -> postService.publishPost(1L));
        assertNotNull(result);
        assertEquals(PostStatus.PUBLISHED, result.getStatus());
    }

    @Test
    void calculateReadTime_LargeContent_ReturnsCorrectTime() {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 401; i++) {
            largeContent.append("word ");
        }
        testPost.setContent(largeContent.toString());
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post result = postService.createPost(testPost);

        assertEquals(3, result.getReadTime()); // 401 words / 200 wpm = 2.005 -> ceil to 3
    }

    @Test
    void updatePost_NotFound_ThrowsException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());
        Post updateData = new Post();
        assertThrows(RuntimeException.class, () -> postService.updatePost(1L, updateData));
    }

    @Test
    void unpublishPost_NotFound_ThrowsException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> postService.unpublishPost(1L));
    }
}
