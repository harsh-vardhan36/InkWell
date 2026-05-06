package com.inkWell.post.resource;
import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.enums.PostStatus;
import com.inkWell.post.service.PostService;
import com.inkWell.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostResource.class)
@AutoConfigureMockMvc(addFilters = false)
class PostResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private PostService postService;

    @Test
    void shouldGetAllPosts() throws Exception {
        when(postRepository.findAllByStatus(PostStatus.PUBLISHED)).thenReturn(List.of(new Post()));
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTrendingPosts() throws Exception {
        when(postRepository.findTop10ByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED)).thenReturn(List.of(new Post()));
        mockMvc.perform(get("/posts/trending"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPostsByAuthor() throws Exception {
        when(postRepository.findAllByAuthorId(1L)).thenReturn(List.of(new Post()));
        mockMvc.perform(get("/posts/author/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPostById() throws Exception {
        Post post = new Post();
        post.setId(1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenPostNotFound() throws Exception {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPostBySlug() throws Exception {
        Post post = new Post();
        post.setSlug("test-post");
        when(postRepository.findBySlug("test-post")).thenReturn(Optional.of(post));
        mockMvc.perform(get("/posts/slug/test-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-post"));
    }

    @Test
    void shouldCreatePost() throws Exception {
        Post post = new Post();
        post.setTitle("New Post");
        when(postService.createPost(any(Post.class))).thenReturn(post);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Post"));
    }

    @Test
    void shouldLikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnlikePost() throws Exception {
        mockMvc.perform(post("/posts/1/unlike"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPublishPost() throws Exception {
        Post post = new Post();
        post.setStatus(PostStatus.PUBLISHED);
        when(postService.publishPost(1L)).thenReturn(post);

        mockMvc.perform(put("/posts/1/publish"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnpublishPost() throws Exception {
        Post post = new Post();
        post.setStatus(PostStatus.DRAFT);
        when(postService.unpublishPost(1L)).thenReturn(post);

        mockMvc.perform(put("/posts/1/unpublish"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdatePost() throws Exception {
        Post post = new Post();
        post.setTitle("Updated Title");
        when(postService.updatePost(anyLong(), any(Post.class))).thenReturn(post);

        mockMvc.perform(put("/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isOk());
    }
}
