package com.inkWell.notification.domain.entity;

import com.inkWell.notification.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Recipient

    private Long actorId; // Who triggered it

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private NotificationType type;

    private String title;

    private String message;

    private Long relatedId;

    @Column(length = 100)
    private String relatedType;

    private boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
