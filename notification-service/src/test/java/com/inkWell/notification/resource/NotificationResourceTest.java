package com.inkWell.notification.resource;
import com.inkWell.notification.domain.entity.Notification;
import com.inkWell.notification.service.NotificationService;
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

@WebMvcTest(NotificationResource.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldCreateNotification() throws Exception {
        Notification notification = new Notification();
        notification.setMessage("Test message");
        when(notificationService.createNotification(any(Notification.class))).thenReturn(notification);

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test message"));
    }

    @Test
    void shouldGetNotificationsByUser() throws Exception {
        when(notificationService.getNotificationsForUser(1L)).thenReturn(List.of(new Notification()));
        mockMvc.perform(get("/notifications/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldMarkAsRead() throws Exception {
        mockMvc.perform(put("/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);
        mockMvc.perform(get("/notifications/user/1/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void shouldMarkAllAsRead() throws Exception {
        mockMvc.perform(put("/notifications/user/1/read-all"))
                .andExpect(status().isOk());
    }
}
