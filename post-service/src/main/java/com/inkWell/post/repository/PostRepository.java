package com.inkWell.post.repository;

import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    List<Post> findAllByStatus(PostStatus status);
    List<Post> findTop10ByStatusOrderByCreatedAtDesc(PostStatus status);
    List<Post> findAllByAuthorId(Long authorId);
    List<Post> findAllByCategoryIdAndStatus(Long categoryId, PostStatus status);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Post> searchPublishedPosts(String query);
    
    @Query("SELECT SUM(p.viewCount) FROM Post p WHERE p.authorId = :authorId")
    Long sumViewCountByAuthorId(Long authorId);

    @Query("SELECT SUM(p.likeCount) FROM Post p WHERE p.authorId = :authorId")
    Long sumLikeCountByAuthorId(Long authorId);

    long countByAuthorId(Long authorId);
    long countByAuthorIdAndStatus(Long authorId, PostStatus status);

    long countByStatus(PostStatus status);
    long countByIsFeaturedTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(Long id);
}
