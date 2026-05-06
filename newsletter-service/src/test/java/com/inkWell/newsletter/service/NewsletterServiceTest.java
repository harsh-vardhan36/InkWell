package com.inkWell.newsletter.service;

import com.inkWell.newsletter.domain.entity.Subscriber;
import com.inkWell.newsletter.domain.enums.SubscriptionStatus;
import com.inkWell.newsletter.repository.SubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NewsletterService newsletterService;

    private Subscriber testSubscriber;

    @BeforeEach
    void setUp() {
        testSubscriber = Subscriber.builder()
                .id(1L)
                .email("test@example.com")
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }

    @Test
    void getActiveSubscribers_ReturnsList() {
        when(subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscriber));

        List<Subscriber> result = newsletterService.getActiveSubscribers();

        assertFalse(result.isEmpty());
        assertEquals("test@example.com", result.get(0).getEmail());
    }

    @Test
    void subscribe_NewUser_Success() {
        when(subscriberRepository.findByEmail("new@ex.com")).thenReturn(Optional.empty());
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        newsletterService.subscribe("new@ex.com");

        verify(subscriberRepository).save(any(Subscriber.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void subscribe_InactiveUser_Reactivates() {
        testSubscriber.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testSubscriber));

        newsletterService.subscribe("test@example.com");

        assertEquals(SubscriptionStatus.ACTIVE, testSubscriber.getStatus());
        verify(subscriberRepository).save(testSubscriber);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void unsubscribe_Success() {
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testSubscriber));

        newsletterService.unsubscribe("test@example.com");

        assertEquals(SubscriptionStatus.UNSUBSCRIBED, testSubscriber.getStatus());
        assertNotNull(testSubscriber.getUnsubscribedAt());
        verify(subscriberRepository).save(testSubscriber);
    }

    @Test
    void unsubscribe_UserNotFound_ThrowsException() {
        when(subscriberRepository.findByEmail("none@ex.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> newsletterService.unsubscribe("none@ex.com"));
    }

    @Test
    void updatePreferences_Success() {
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testSubscriber));

        newsletterService.updatePreferences("test@example.com", "tech,news");

        assertEquals("tech,news", testSubscriber.getPreferences());
        verify(subscriberRepository).save(testSubscriber);
    }

    @Test
    void notifyNewPost_SendsEmails() {
        when(subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscriber));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        newsletterService.notifyNewPost("Post Title", "http://link.com");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void notifyNewPost_NoSubscribers_DoesNothing() {
        when(subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of());

        newsletterService.notifyNewPost("Title", "Link");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void notifyNewPost_MailFailure_Continues() {
        when(subscriberRepository.findAllByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(testSubscriber));
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(MimeMessage.class));

        // Should not throw exception
        assertDoesNotThrow(() -> newsletterService.notifyNewPost("Title", "Link"));
    }
}
