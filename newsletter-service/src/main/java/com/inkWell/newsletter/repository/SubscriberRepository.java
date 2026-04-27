package com.inkWell.newsletter.repository;

import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    Optional<Subscriber> findByConfirmationToken(String token);
    List<Subscriber> findAllByStatus(SubscriptionStatus status);
}
