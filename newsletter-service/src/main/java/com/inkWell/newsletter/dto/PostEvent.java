package com.inkWell.newsletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object representing a post-published event.
 * Received from RabbitMQ when a new post is published in the post-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent implements Serializable {
    private Long postId;
    private String title;
    private String authorName;
    private String categoryName;
    private String slug;
}
