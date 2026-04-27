package com.inkWell.admin.resource;

import com.inkWell.admin.client.*;
import com.inkWell.admin.dto.UserDto;
import com.inkWell.admin.security.JwtUtil;
import com.inkWell.admin.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceClient authServiceClient;
    @MockitoBean
    private PostServiceClient postServiceClient;
    @MockitoBean
    private CommentServiceClient commentServiceClient;
    @MockitoBean
    private CategoryServiceClient categoryServiceClient;
    @MockitoBean
    private NewsletterServiceClient newsletterServiceClient;
    @MockitoBean
    private NotificationServiceClient notificationServiceClient;
    @MockitoBean
    private AuditService auditService;
    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void shouldReturnUserList() throws Exception {
        List<UserDto> users = new ArrayList<>();
        when(authServiceClient.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAdminDashboard() throws Exception {
        when(authServiceClient.getAllUsers()).thenReturn(Collections.emptyList());
        when(postServiceClient.getAllPosts()).thenReturn(Collections.emptyList());
        when(commentServiceClient.getAllComments()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAuditLogs() throws Exception {
        when(auditService.getAllLogs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/audit-logs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
