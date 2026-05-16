package com.inkWell.newsletter.resource;

import com.inkWell.newsletter.dto.SubscriberDTO;
import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for managing newsletter-related operations.
 * Provides endpoints for subscription, unsubscription, and preference management.
 */
@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterResource {

    private final NewsletterService newsletterService;

    /**
     * Retrieves a list of all active newsletter subscribers.
     * 
     * @return A {@link ResponseEntity} containing a list of active {@link Subscriber}s.
     */
    @GetMapping("/subscribers")
    public ResponseEntity<java.util.List<SubscriberDTO>> getActiveSubscribers(@RequestParam(required = false) Long authorId) {
        return ResponseEntity.ok(newsletterService.getActiveSubscribers(authorId).stream()
                .map(this::convertToDTO)
                .toList());
    }

    /**
     * Gets the active subscriber count for a specific author.
     */
    @GetMapping("/subscribers/count")
    public ResponseEntity<Long> getSubscribersCount(@RequestParam Long authorId) {
        return ResponseEntity.ok(newsletterService.getSubscribersCount(authorId));
    }

    private SubscriberDTO convertToDTO(Subscriber subscriber) {
        return SubscriberDTO.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .status(subscriber.getStatus())
                .preferences(subscriber.getPreferences())
                .subscribedAt(subscriber.getSubscribedAt())
                .build();
    }

    /**
     * Subscribes a user to the newsletter using their email address.
     * 
     * @param email The email address to subscribe.
     * @return A {@link ResponseEntity} with a success message.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestParam String email, @RequestParam Long authorId) {
        newsletterService.subscribe(email, authorId);
        return ResponseEntity.ok(Map.of("message", "Subscribed successfully"));
    }


    /**
     * Triggers a newsletter notification for a new post.
     * 
     * @param request A {@link com.inkWell.newsletter.dto.NewsletterRequest} containing post details.
     * @return A {@link ResponseEntity} with NO_CONTENT status upon success.
     */
    @PostMapping("/notify-new-post")
    public ResponseEntity<Map<String, String>> notifyNewPost(@RequestBody com.inkWell.newsletter.dto.NewsletterRequest request) {
        newsletterService.notifyNewPost(request.getTitle(), request.getLink(), request.getAuthorId());
        return ResponseEntity.ok(Map.of("message", "Campaign launched successfully"));
    }

    /**
     * Unsubscribes a user from the newsletter.
     * 
     * @param email The email address to unsubscribe.
     * @return A {@link ResponseEntity} with a success message.
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String email) {
        newsletterService.unsubscribe(email);
        return ResponseEntity.ok(Map.of("message", "Unsubscribed successfully"));
    }

    /**
     * Updates the newsletter preferences for a specific subscriber.
     * 
     * @param email The subscriber's email.
     * @param preferences The new preference settings.
     * @return A {@link ResponseEntity} with a success message.
     */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(@RequestParam String email, @RequestParam String preferences) {
        newsletterService.updatePreferences(email, preferences);
        return ResponseEntity.ok(Map.of("message", "Preferences updated"));
    }
}
