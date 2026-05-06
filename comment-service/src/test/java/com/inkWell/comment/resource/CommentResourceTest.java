package com.inkWell.comment.resource;
import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentResource.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    void shouldGetCommentsByPost() throws Exception {
        when(commentService.getCommentsByPost(1L)).thenReturn(List.of(new Comment()));
        mockMvc.perform(get("/comments/post/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCommentCount() throws Exception {
        when(commentService.getCommentCount(1L)).thenReturn(5L);
        mockMvc.perform(get("/comments/post/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void shouldAddComment() throws Exception {
        Comment comment = new Comment();
        comment.setContent("Nice post!");
        when(commentService.addComment(any(Comment.class))).thenReturn(comment);

        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Nice post!"));
    }

    @Test
    void shouldGetReplies() throws Exception {
        when(commentService.getReplies(1L)).thenReturn(List.of(new Comment()));
        mockMvc.perform(get("/comments/1/replies"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLikeComment() throws Exception {
        mockMvc.perform(post("/comments/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnlikeComment() throws Exception {
        mockMvc.perform(post("/comments/1/unlike"))
                .andExpect(status().isOk());
    }
}
