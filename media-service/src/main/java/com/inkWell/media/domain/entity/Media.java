package com.inkWell.media.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private String originalName;

    @Column(nullable = false)
    private String fileUrl;

    private String mimeType;

    private Long sizeKb;

    private Long uploaderId;

    @Builder.Default
    private boolean isDeleted = false;

    private String altText;

    private Long linkedPostId;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
