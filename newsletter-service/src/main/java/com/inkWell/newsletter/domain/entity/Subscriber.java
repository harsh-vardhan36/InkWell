package com.inkWell.newsletter.domain.entity;

import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a newsletter subscriber.
 * Stores subscriber email, status, preferences, and timestamps.
 */
@Entity
@Table(name = "subscribers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    /**
     * Unique identifier for the subscriber.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique email address of the subscriber.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Current status of the subscription (e.g., ACTIVE, UNSUBSCRIBED).
     */
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    /**
     * Token used for email confirmation (if implemented).
     */
    private String confirmationToken;

    /**
     * Expiration date/time for the confirmation token.
     */
    private LocalDateTime tokenExpiry;

    /**
     * Comma-separated tags or keywords representing the subscriber's interests.
     */
    private String preferences; // Comma-separated tags

    /**
     * Unique identifier of the author this subscriber is following.
     */
    private Long authorId;

    /**
     * Timestamp when the subscriber first joined.
     */
    @CreationTimestamp
    private LocalDateTime subscribedAt;

    /**
     * Timestamp when the subscriber opted out.
     */
    private LocalDateTime unsubscribedAt;
}
