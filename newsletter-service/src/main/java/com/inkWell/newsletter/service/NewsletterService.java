package com.inkWell.newsletter.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import com.inkWell.newsletter.repository.SubscriberRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing newsletter subscriptions and campaigns.
 * Handles subscriber registration, unsubscription, preference updates, and 
 * sending various types of emails (welcome, new post notifications).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    /**
     * Repository for managing subscriber data persistence.
     */
    private final SubscriberRepository subscriberRepository;

    /**
     * Spring mail sender for dispatching newsletter and notification emails.
     */
    private final JavaMailSender mailSender;

    /**
     * Retrieves all currently active subscribers for a specific author.
     * 
     * @param authorId The ID of the author to filter by.
     * @return A list of {@link Subscriber} objects with ACTIVE status for that author.
     */
    public List<Subscriber> getActiveSubscribers(Long authorId) {
        if (authorId == null) {
            return subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE);
        }
        return subscriberRepository.findAllByStatusAndAuthorId(SubscriptionStatus.ACTIVE, authorId);
    }

    /**
     * Gets the count of active subscribers for an author.
     */
    public long getSubscribersCount(Long authorId) {
        return subscriberRepository.countByStatusAndAuthorId(SubscriptionStatus.ACTIVE, authorId);
    }

    /**
     * Subscribes a new email address to an author's newsletter.
     * 
     * @param email The email address to subscribe.
     * @param authorId The ID of the author.
     */
    public void subscribe(String email, Long authorId) {
        Subscriber subscriber = subscriberRepository.findByEmailAndAuthorId(email, authorId).orElse(null);
        if (subscriber == null) {
            subscriber = Subscriber.builder()
                    .email(email)
                    .authorId(authorId)
                    .status(SubscriptionStatus.ACTIVE)
                    .build();
            subscriberRepository.save(subscriber);
            try {
                sendWelcomeEmail(email);
            } catch (Exception e) {
                log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
            }
        } else if (subscriber.getStatus() != SubscriptionStatus.ACTIVE) {
            subscriber.setStatus(SubscriptionStatus.ACTIVE);
            subscriberRepository.save(subscriber);
        }
    }

    /**
     * Unsubscribes an existing subscriber from the newsletter.
     * Updates the status to UNSUBSCRIBED and records the timestamp.
     * 
     * @param email The email address to unsubscribe.
     * @throws RuntimeException if the subscriber is not found.
     */
    public void unsubscribe(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        subscriber.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    /**
     * Updates the preferences for a subscriber.
     * 
     * @param email The subscriber's email.
     * @param preferences The new preference string (e.g., comma-separated tags).
     * @throws RuntimeException if the subscriber is not found.
     */
    public void updatePreferences(String email, String preferences) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        subscriber.setPreferences(preferences);
        subscriberRepository.save(subscriber);
    }

    /**
     * Notifies all active subscribers about a new post.
     * Iterates through the list of active subscribers and sends a notification email to each.
     * 
     * @param title The title of the new post.
     * @param link The direct link to the post.
     */
    public void notifyNewPost(String title, String link, Long authorId) {
        List<Subscriber> activeSubscribers = getActiveSubscribers(authorId);
        log.info("Starting newsletter campaign for '{}' by author {}. Targeting {} active subscribers.", title, authorId, activeSubscribers.size());
        
        if (activeSubscribers.isEmpty()) {
            log.warn("No active subscribers found for newsletter campaign.");
            return;
        }

        for (Subscriber s : activeSubscribers) {
            try {
                sendNewPostEmail(s.getEmail(), title, link);
                log.info("Newsletter sent successfully to {}", s.getEmail());
            } catch (Exception e) {
                log.error("Failed to send newsletter to {}: {}", s.getEmail(), e.getMessage());
            }
        }
    }


    /**
     * Sends a welcome email to a new subscriber.
     * Uses a rich HTML template with InkWell branding.
     * 
     * @param email The recipient's email address.
     */
    private void sendWelcomeEmail(String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@inkwell.app", "InkWell");
            helper.setTo(email);
            helper.setSubject("Welcome to InkWell! ✍️");
            
            String html = "<!DOCTYPE html>" +
                "<html><body style='font-family: sans-serif; background-color: #0f0f1a; padding: 40px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background: linear-gradient(145deg,#1a1a2e,#16213e); border-radius: 16px; overflow: hidden; border: 1px solid rgba(139,92,246,0.3);'>" +
                "    <div style='background: linear-gradient(135deg,#6d28d9,#4f46e5); padding: 40px; text-align: center; color: white;'>" +
                "      <h1 style='margin: 0; font-size: 32px;'>Welcome to InkWell</h1>" +
                "    </div>" +
                "    <div style='padding: 40px; color: #e2e8f0;'>" +
                "      <h2 style='color: #c4b5fd;'>Your subscription is now active!</h2>" +
                "      <p style='color: #94a3b8; line-height: 1.6;'>You've successfully joined our community. You'll now receive exclusive newsletters and updates directly in your inbox.</p>" +
                "      <div style='margin-top: 30px; border-top: 1px solid #2d3748; padding-top: 20px; color: #718096; font-size: 14px;'>" +
                "        Thanks for joining,<br>The InkWell Team" +
                "      </div>" +
                "    </div>" +
                "  </div>" +
                "</body></html>";
            
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }

    /**
     * Sends a new post notification email to a subscriber.
     * Uses a rich HTML template including the post title and a link to the full story.
     * 
     * @param email The recipient's email address.
     * @param title The title of the post.
     * @param link The link to the post.
     */
    private void sendNewPostEmail(String email, String title, String link) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@inkwell.app", "InkWell");
            helper.setTo(email);
            helper.setSubject("New Story: " + title + " 📖");

            String html = "<!DOCTYPE html>" +
                "<html><body style='font-family: sans-serif; background-color: #0f0f1a; padding: 40px;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background: #1a1a2e; border-radius: 16px; overflow: hidden; border: 1px solid rgba(139,92,246,0.3);'>" +
                "    <div style='background: linear-gradient(135deg,#6d28d9,#4f46e5); padding: 30px; text-align: center; color: white;'>" +
                "      <div style='font-size: 24px; font-weight: bold;'>Fresh from InkWell</div>" +
                "    </div>" +
                "    <div style='padding: 40px; color: #e2e8f0;'>" +
                "      <h1 style='color: #ffffff; margin-top: 0;'>" + title + "</h1>" +
                "      <p style='color: #94a3b8; line-height: 1.6; font-size: 16px;'>A new story has just been published on InkWell that we think you'll love.</p>" +
                "      <a href='" + link + "' style='display: inline-block; background: #6d28d9; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: bold; margin-top: 20px;'>Read the full story</a>" +
                "      <div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #2d3748; font-size: 12px; color: #718096;'>" +
                "        If you no longer wish to receive these emails, you can <a href='#' style='color: #6d28d9;'>unsubscribe here</a>." +
                "      </div>" +
                "    </div>" +
                "  </div>" +
                "</body></html>";

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send newsletter email to {}: {}", email, e.getMessage());
        }
    }
}
