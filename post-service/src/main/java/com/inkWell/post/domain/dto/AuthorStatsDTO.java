package com.inkWell.post.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorStatsDTO {
    private Long totalPosts;
    private Long publishedPosts;
    private Long totalViews;
    private Long totalLikes;
    private Long totalBookmarks; // Placeholder for now
}
