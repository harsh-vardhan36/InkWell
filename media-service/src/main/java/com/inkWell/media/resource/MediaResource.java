package com.inkWell.media.resource;

import com.inkWell.media.domain.entity.Media;
import com.inkWell.media.dto.MediaDTO;
import com.inkWell.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaResource {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaDTO> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws IOException {
        return ResponseEntity.ok(convertToDTO(mediaService.uploadMedia(file, userId)));
    }

    @PostMapping("/upload-url")
    public ResponseEntity<MediaDTO> uploadFromUrl(
            @RequestParam("url") String url,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws IOException {
        return ResponseEntity.ok(convertToDTO(mediaService.uploadFromUrl(url, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/alt")
    public ResponseEntity<MediaDTO> updateAltText(@PathVariable Long id, @RequestParam String altText) {
        return ResponseEntity.ok(convertToDTO(mediaService.updateAltText(id, altText)));
    }

    @PutMapping("/{id}/link/{postId}")
    public ResponseEntity<Void> linkToPost(@PathVariable Long id, @PathVariable Long postId) {
        mediaService.linkToPost(id, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<java.util.List<MediaDTO>> getMediaByUploader(@PathVariable Long userId) {
        return ResponseEntity.ok(mediaService.getMediaByUploader(userId).stream()
                .map(this::convertToDTO)
                .toList());
    }

    private MediaDTO convertToDTO(Media media) {
        return MediaDTO.builder()
                .id(media.getId())
                .fileUrl(media.getFileUrl())
                .fileName(media.getFileName())
                .originalName(media.getOriginalName())
                .mimeType(media.getMimeType())
                .sizeKb(media.getSizeKb())
                .uploaderId(media.getUploaderId())
                .altText(media.getAltText())
                .linkedPostId(media.getLinkedPostId())
                .uploadedAt(media.getUploadedAt())
                .build();
    }
}
