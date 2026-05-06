package com.inkWell.comment.domain.entity;

import com.inkWell.comment.domain.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long authorId;

    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Long parentCommentId; // For nested comments

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    private boolean isApproved;

    private boolean isDeleted;

    @Builder.Default
    private int likes = 0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public CommentStatus getStatus() { return status; }
    public void setStatus(CommentStatus status) { this.status = status; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean isApproved) { this.isApproved = isApproved; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public static CommentBuilder builder() {
        return new CommentBuilder();
    }

    public static class CommentBuilder {
        private Comment comment = new Comment();
        public CommentBuilder id(Long id) { comment.id = id; return this; }
        public CommentBuilder postId(Long postId) { comment.postId = postId; return this; }
        public CommentBuilder authorId(Long authorId) { comment.authorId = authorId; return this; }
        public CommentBuilder authorName(String authorName) { comment.authorName = authorName; return this; }
        public CommentBuilder content(String content) { comment.content = content; return this; }
        public CommentBuilder parentCommentId(Long parentCommentId) { comment.parentCommentId = parentCommentId; return this; }
        public CommentBuilder status(CommentStatus status) { comment.status = status; return this; }
        public CommentBuilder isApproved(boolean isApproved) { comment.isApproved = isApproved; return this; }
        public Comment build() { return comment; }
    }
}
