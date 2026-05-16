package com.inkWell.newsletter.resource;
import com.inkWell.newsletter.service.NewsletterService;
import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.dto.NewsletterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsletterResource.class)
@AutoConfigureMockMvc(addFilters = false)
class NewsletterResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsletterService newsletterService;

    @Test
    void shouldGetActiveSubscribers() throws Exception {
        when(newsletterService.getActiveSubscribers(null)).thenReturn(List.of(new Subscriber()));
        mockMvc.perform(get("/newsletter/subscribers"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSubscribe() throws Exception {
        mockMvc.perform(post("/newsletter/subscribe")
                .param("email", "test@example.com")
                .param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Subscribed successfully"));
    }

    @Test
    void shouldNotifyNewPost() throws Exception {
        NewsletterRequest request = new NewsletterRequest();
        request.setTitle("New Post");
        request.setLink("http://link.com");

        mockMvc.perform(post("/newsletter/notify-new-post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUnsubscribe() throws Exception {
        mockMvc.perform(post("/newsletter/unsubscribe")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unsubscribed successfully"));
    }

    @Test
    void shouldUpdatePreferences() throws Exception {
        mockMvc.perform(put("/newsletter/preferences")
                .param("email", "test@example.com")
                .param("preferences", "weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Preferences updated"));
    }
}
