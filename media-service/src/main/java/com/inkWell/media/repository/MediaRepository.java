package com.inkWell.media.repository;

import com.inkWell.media.domain.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByUploaderId(Long uploaderId);
    List<Media> findByLinkedPostId(Long postId);
    List<Media> findByIsDeleted(boolean isDeleted);
}
