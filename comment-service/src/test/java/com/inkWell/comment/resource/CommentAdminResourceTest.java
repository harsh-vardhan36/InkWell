package com.inkWell.comment.resource;
import com.inkWell.comment.domain.entity.Comment;
import com.inkWell.comment.service.CommentService;
import com.inkWell.comment.repository.CommentRepository;
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

@WebMvcTest(CommentAdminResource.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentAdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private CommentService commentService;

    @Test
    void shouldGetPendingComments() throws Exception {
        when(commentService.getAllComments()).thenReturn(List.of(new Comment()));
        mockMvc.perform(get("/comments/admin/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveComment() throws Exception {
        when(commentService.approveComment(1L)).thenReturn(new Comment());
        mockMvc.perform(put("/comments/admin/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectComment() throws Exception {
        when(commentService.rejectComment(1L)).thenReturn(new Comment());
        mockMvc.perform(put("/comments/admin/1/reject"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHardDeleteComment() throws Exception {
        mockMvc.perform(delete("/comments/admin/1/permanent"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateStatus() throws Exception {
        Comment comment = new Comment();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        mockMvc.perform(put("/comments/admin/1/status")
                .param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSoftDeleteComment() throws Exception {
        mockMvc.perform(delete("/comments/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully (soft-delete)"));
    }
}
