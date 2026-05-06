package com.inkWell.post.domain.entity;

import com.inkWell.post.domain.enums.PostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a blog post.
 * Stores post content, metadata, author information, and engagement statistics.
 */
@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    /**
     * Unique identifier for the post.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier of the author who created the post.
     */
    @Column(nullable = false)
    private Long authorId;

    /**
     * Display name of the author.
     */
    private String authorName;

    /**
     * Unique identifier of the category this post belongs to.
     */
    private Long categoryId;

    /**
     * The title of the blog post.
     */
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * URL-friendly version of the title.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String slug;

    /**
     * The main body content of the post (HTML/Markdown).
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * A short summary or teaser of the post content.
     */
    @Column(columnDefinition = "TEXT")
    private String excerpt;

    /**
     * URL to the featured image for the post.
     */
    private String featuredImageUrl;

    /**
     * Estimated time to read the post in minutes.
     */
    private int readTime;

    /**
     * Current status of the post (e.g., DRAFT, PUBLISHED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    /**
     * Whether the post is featured on the homepage.
     */
    private boolean isFeatured;

    /**
     * Total number of views.
     */
    private int viewCount;

    /**
     * Total number of likes.
     */
    private int likeCount;

    /**
     * Total number of comments.
     */
    private int commentCount;

    /**
     * Timestamp when the post was officially published.
     */
    private LocalDateTime publishedAt;

    /**
     * Timestamp when the post record was created.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp when the post record was last updated.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getFeaturedImageUrl() { return featuredImageUrl; }
    public void setFeaturedImageUrl(String featuredImageUrl) { this.featuredImageUrl = featuredImageUrl; }
    public int getReadTime() { return readTime; }
    public void setReadTime(int readTime) { this.readTime = readTime; }
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PostBuilder builder() {
        return new PostBuilder();
    }

    public static class PostBuilder {
        private Post post = new Post();
        public PostBuilder id(Long id) { post.id = id; return this; }
        public PostBuilder authorId(Long authorId) { post.authorId = authorId; return this; }
        public PostBuilder authorName(String authorName) { post.authorName = authorName; return this; }
        public PostBuilder categoryId(Long categoryId) { post.categoryId = categoryId; return this; }
        public PostBuilder title(String title) { post.title = title; return this; }
        public PostBuilder slug(String slug) { post.slug = slug; return this; }
        public PostBuilder content(String content) { post.content = content; return this; }
        public PostBuilder excerpt(String excerpt) { post.excerpt = excerpt; return this; }
        public PostBuilder featuredImageUrl(String featuredImageUrl) { post.featuredImageUrl = featuredImageUrl; return this; }
        public PostBuilder readTime(int readTime) { post.readTime = readTime; return this; }
        public PostBuilder status(PostStatus status) { post.status = status; return this; }
        public Post build() { return post; }
    }
}
