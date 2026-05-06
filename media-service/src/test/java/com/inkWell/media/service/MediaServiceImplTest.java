package com.inkWell.media.service;

import com.inkWell.media.domain.entity.Media;
import com.inkWell.media.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private Media testMedia;

    @BeforeEach
    void setUp() {
        testMedia = Media.builder()
                .id(1L)
                .fileName("test.jpg")
                .fileUrl("http://s3.com/test.jpg")
                .uploaderId(100L)
                .isDeleted(false)
                .build();
    }

    @Test
    void uploadMedia_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

        Media result = mediaService.uploadMedia(file, 100L);

        assertNotNull(result);
        assertEquals("test.jpg", result.getOriginalName());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void uploadFromUrl_Success() throws IOException {
        byte[] content = "content".getBytes();
        ResponseEntity<byte[]> response = new ResponseEntity<>(content, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(byte[].class))).thenReturn(response);
        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

        Media result = mediaService.uploadFromUrl("http://example.com/img.jpg", 100L);

        assertNotNull(result);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFromUrl_Failure_ThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        assertThrows(IOException.class, () -> mediaService.uploadFromUrl("http://bad-url.com", 100L));
    }

    @Test
    void getMediaById_Success() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        assertTrue(mediaService.getMediaById(1L).isPresent());
    }

    @Test
    void getMediaByUploader_Success() {
        when(mediaRepository.findByUploaderId(100L)).thenReturn(Arrays.asList(testMedia));
        assertFalse(mediaService.getMediaByUploader(100L).isEmpty());
    }

    @Test
    void getMediaByPost_Success() {
        when(mediaRepository.findByLinkedPostId(500L)).thenReturn(Arrays.asList(testMedia));
        assertFalse(mediaService.getMediaByPost(500L).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteMedia_Success() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));

        mediaService.deleteMedia(1L);

        assertTrue(testMedia.isDeleted());
        verify(s3Client).deleteObject(any(java.util.function.Consumer.class));
        verify(mediaRepository).save(testMedia);
    }

    @Test
    void updateAltText_Success() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        mediaService.updateAltText(1L, "New Alt Text");
        assertEquals("New Alt Text", testMedia.getAltText());
        verify(mediaRepository).save(testMedia);
    }

    @Test
    void linkToPost_Success() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));

        mediaService.linkToPost(1L, 500L);

        assertEquals(500L, testMedia.getLinkedPostId());
        verify(mediaRepository).save(testMedia);
    }

    @Test
    void unlinkFromPost_Success() {
        testMedia.setLinkedPostId(500L);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));

        mediaService.unlinkFromPost(1L);

        assertNull(testMedia.getLinkedPostId());
        verify(mediaRepository).save(testMedia);
    }

    @Test
    void getAllMedia_Success() {
        when(mediaRepository.findAll()).thenReturn(Arrays.asList(testMedia));
        assertEquals(1, mediaService.getAllMedia().size());
    }

    @Test
    void cleanupDeleted_Success() {
        when(mediaRepository.findByIsDeleted(true)).thenReturn(Arrays.asList(testMedia));
        mediaService.cleanupDeleted();
        verify(mediaRepository).deleteAll(anyList());
    }
}
