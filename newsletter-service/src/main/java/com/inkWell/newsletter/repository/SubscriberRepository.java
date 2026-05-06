package com.inkWell.newsletter.repository;

import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Subscriber} entities.
 * Provides methods for finding subscribers by email, confirmation token, and status.
 */
@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    /**
     * Finds a subscriber by their unique email address.
     * 
     * @param email The email to search for.
     * @return An {@link Optional} containing the subscriber if found.
     */
    Optional<Subscriber> findByEmail(String email);

    /**
     * Finds a subscriber by their confirmation token.
     * 
     * @param token The confirmation token.
     * @return An {@link Optional} containing the subscriber if found.
     */
    Optional<Subscriber> findByConfirmationToken(String token);

    /**
     * Finds all subscribers with a specific subscription status.
     * 
     * @param status The {@link SubscriptionStatus} to filter by.
     * @return A list of matching subscribers.
     */
    List<Subscriber> findAllByStatus(SubscriptionStatus status);
}
