package com.inkWell.media.resource;

import com.inkWell.media.domain.entity.Media;
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
    public ResponseEntity<Media> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) throws IOException {
        return ResponseEntity.ok(mediaService.uploadMedia(file, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/alt")
    public ResponseEntity<Media> updateAltText(@PathVariable Long id, @RequestParam String altText) {
        return ResponseEntity.ok(mediaService.updateAltText(id, altText));
    }

    @PutMapping("/{id}/link/{postId}")
    public ResponseEntity<Void> linkToPost(@PathVariable Long id, @PathVariable Long postId) {
        mediaService.linkToPost(id, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<java.util.List<Media>> getMediaByUploader(@PathVariable Long userId) {
        return ResponseEntity.ok(mediaService.getMediaByUploader(userId));
    }
}
