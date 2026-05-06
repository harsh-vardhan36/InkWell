package com.inkWell.media.resource;

import com.inkWell.media.domain.entity.Media;
import com.inkWell.media.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaResource.class)
@AutoConfigureMockMvc(addFilters = false)
class MediaResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    void shouldUploadMedia() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Media media = Media.builder().id(1L).fileUrl("http://url.com").build();
        when(mediaService.uploadMedia(any(), anyLong())).thenReturn(media);

        mockMvc.perform(multipart("/media/upload")
                .file(file)
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value("http://url.com"));
    }

    @Test
    void shouldUploadFromUrl() throws Exception {
        Media media = Media.builder().id(1L).fileUrl("http://url.com").build();
        when(mediaService.uploadFromUrl(anyString(), anyLong())).thenReturn(media);

        mockMvc.perform(post("/media/upload-url")
                .param("url", "http://image.com/test.jpg")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value("http://url.com"));
    }

    @Test
    void shouldDeleteMedia() throws Exception {
        mockMvc.perform(delete("/media/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateAltText() throws Exception {
        Media media = Media.builder().id(1L).altText("New Alt").build();
        when(mediaService.updateAltText(anyLong(), anyString())).thenReturn(media);

        mockMvc.perform(put("/media/1/alt")
                .param("altText", "New Alt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.altText").value("New Alt"));
    }

    @Test
    void shouldLinkToPost() throws Exception {
        mockMvc.perform(put("/media/1/link/100"))
                .andExpect(status().isOk());
    }
}
