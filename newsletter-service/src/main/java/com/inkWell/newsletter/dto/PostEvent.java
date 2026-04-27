package com.inkWell.newsletter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent implements Serializable {
    private Long postId;
    private String title;
    private String authorName;
    private String categoryName;
}
