package com.inkWell.comment.repository;

import com.inkWell.comment.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByPostIdAndIsApprovedTrue(Long postId);
    List<Comment> findByParentCommentId(Long parentId);
    long countByPostId(Long postId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.id = :id")
    void incrementLikeCount(Long id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Comment c SET c.likes = c.likes - 1 WHERE c.id = :id")
    void decrementLikeCount(Long id);
}
