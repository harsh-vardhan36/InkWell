package com.inkWell.newsletter.resource;

import com.inkWell.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterResource {

    private final NewsletterService newsletterService;

    @GetMapping("/subscribers")
    public ResponseEntity<java.util.List<com.inkWell.newsletter.domain.entity.Subscriber>> getActiveSubscribers() {
        return ResponseEntity.ok(newsletterService.getActiveSubscribers());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestParam String email) {
        newsletterService.subscribe(email);
        return ResponseEntity.ok(Map.of("message", "Confirmation email sent"));
    }

    @GetMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirm(@RequestParam String token) {
        newsletterService.confirm(token);
        return ResponseEntity.ok(Map.of("message", "Subscription active"));
    }

    @PostMapping("/notify-new-post")
    public ResponseEntity<Void> notifyNewPost(@RequestParam String title, @RequestParam String link) {
        newsletterService.notifyNewPost(title, link);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam String email) {
        newsletterService.unsubscribe(email);
        return ResponseEntity.ok(Map.of("message", "Unsubscribed successfully"));
    }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(@RequestParam String email, @RequestParam String preferences) {
        newsletterService.updatePreferences(email, preferences);
        return ResponseEntity.ok(Map.of("message", "Preferences updated"));
    }
}
