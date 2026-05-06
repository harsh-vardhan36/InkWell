package com.inkWell.post.resource;
import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.service.PostService;
import com.inkWell.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostAdminResource.class)
@AutoConfigureMockMvc(addFilters = false)
class PostAdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private PostService postService;

    @Test
    void shouldGetAllPosts() throws Exception {
        when(postRepository.findAll()).thenReturn(List.of(new Post()));
        mockMvc.perform(get("/posts/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldToggleFeature() throws Exception {
        Post post = new Post();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        mockMvc.perform(put("/posts/admin/1/feature")
                .param("featured", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPublishPost() throws Exception {
        when(postService.publishPost(1L)).thenReturn(new Post());
        mockMvc.perform(put("/posts/admin/1/publish"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnpublishPost() throws Exception {
        when(postService.unpublishPost(1L)).thenReturn(new Post());
        mockMvc.perform(put("/posts/admin/1/unpublish"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully"));
    }

    @Test
    void shouldGetPostStats() throws Exception {
        when(postRepository.count()).thenReturn(10L);
        mockMvc.perform(get("/posts/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPosts").value(10));
    }
}
