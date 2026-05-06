package com.inkWell.notification.service;

import com.inkWell.notification.domain.entity.Notification;
import com.inkWell.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(100L);
        testNotification.setMessage("Test message");
        testNotification.setRead(false);
    }

    @Test
    void createNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification created = notificationService.createNotification(testNotification);

        assertFalse(created.isRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void getNotificationsForUser_Success() {
        when(notificationRepository.findAllByUserIdOrderByCreatedAtDesc(100L)).thenReturn(Arrays.asList(testNotification));

        List<Notification> result = notificationService.getNotificationsForUser(100L);

        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getMessage());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        notificationService.markAsRead(1L);

        assertTrue(testNotification.isRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(100L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(100L);

        assertEquals(5L, count);
    }

    @Test
    void readAllForUser_Success() {
        Notification n2 = new Notification();
        n2.setRead(false);
        when(notificationRepository.findAllByUserIdAndIsReadFalse(100L)).thenReturn(Arrays.asList(testNotification, n2));

        notificationService.readAllForUser(100L);

        assertTrue(testNotification.isRead());
        assertTrue(n2.isRead());
        verify(notificationRepository).saveAll(any());
    }
}
