package com.inkWell.newsletter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import com.inkWell.newsletter.repository.SubscriberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final SubscriberRepository subscriberRepository;
    private final JavaMailSender mailSender;

    public List<Subscriber> getActiveSubscribers() {
        return subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE);
    }

    public void subscribe(String email) {
        String token = UUID.randomUUID().toString();
        Subscriber subscriber = Subscriber.builder()
                .email(email)
                .status(SubscriptionStatus.PENDING)
                .confirmationToken(token)
                .tokenExpiry(LocalDateTime.now().plusHours(24))
                .build();
        subscriberRepository.save(subscriber);
        try {
            sendConfirmationEmail(email, token);
        } catch (Exception e) {
            // Log error
        }
    }

    public void confirm(String token) {
        Subscriber subscriber = subscriberRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        if (subscriber.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        subscriber.setStatus(SubscriptionStatus.ACTIVE);
        subscriber.setConfirmationToken(null);
        subscriberRepository.save(subscriber);
        try {
            sendWelcomeEmail(subscriber.getEmail());
        } catch (Exception e) {
            // Log error
        }
    }

    public void unsubscribe(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        subscriber.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(subscriber);
    }

    public void updatePreferences(String email, String preferences) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
        subscriber.setPreferences(preferences);
        subscriberRepository.save(subscriber);
    }

    public void notifyNewPost(String title, String link) {
        List<Subscriber> activeSubscribers = subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE);
        for (Subscriber s : activeSubscribers) {
            try {
                sendNewPostEmail(s.getEmail(), title, link);
            } catch (Exception e) {
                // Log error
            }
        }
    }

    private void sendConfirmationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirm your InkWell subscription");
        message.setText("Click here to confirm: http://localhost:8080/api/newsletter/confirm?token=" + token);
        mailSender.send(message);
    }

    private void sendWelcomeEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome to InkWell!");
        message.setText("Your subscription is now active.");
        mailSender.send(message);
    }

    private void sendNewPostEmail(String email, String title, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("New Post: " + title);
        message.setText("Read it here: " + link);
        mailSender.send(message);
    }
}
