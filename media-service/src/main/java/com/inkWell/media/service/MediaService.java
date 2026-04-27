package com.inkWell.media.service;

import com.inkWell.media.domain.entity.Media;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface MediaService {
    Media uploadMedia(MultipartFile file, Long uploaderId) throws IOException;
    Optional<Media> getMediaById(Long id);
    List<Media> getMediaByUploader(Long uploaderId);
    List<Media> getMediaByPost(Long postId);
    void deleteMedia(Long id);
    Media updateAltText(Long id, String altText);
    void linkToPost(Long mediaId, Long postId);
    void unlinkFromPost(Long mediaId);
    List<Media> getAllMedia();
    void cleanupDeleted();
}
