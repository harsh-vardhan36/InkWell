package com.inkWell.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    private Long id;
    private String fileUrl;
    private String fileName;
    private String originalName;
    private String mimeType;
    private Long sizeKb;
    private Long uploaderId;
    private String altText;
    private Long linkedPostId;
    private LocalDateTime uploadedAt;
}
