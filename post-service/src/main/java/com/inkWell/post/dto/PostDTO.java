package com.inkWell.post.dto;

import com.inkWell.post.domain.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String featuredImageUrl;
    private int readTime;
    private PostStatus status;
    private boolean isFeatured;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
