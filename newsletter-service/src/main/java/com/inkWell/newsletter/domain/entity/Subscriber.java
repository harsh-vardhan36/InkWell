package com.inkWell.newsletter.domain.entity;

import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private String confirmationToken;

    private LocalDateTime tokenExpiry;

    private String preferences; // Comma-separated tags

    @CreationTimestamp
    private LocalDateTime subscribedAt;

    private LocalDateTime unsubscribedAt;
}
