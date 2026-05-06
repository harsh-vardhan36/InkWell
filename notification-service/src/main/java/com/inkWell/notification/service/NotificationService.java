package com.inkWell.notification.service;

import com.inkWell.notification.domain.entity.Notification;
import com.inkWell.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing user notifications.
 * Handles creation, retrieval, and read-status management of in-app notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a new notification for a user.
     * By default, the notification is marked as unread.
     * 
     * @param notification The notification entity to save.
     * @return The saved {@link Notification} entity.
     */
    public Notification createNotification(Notification notification) {
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    /**
     * Retrieves all notifications for a specific user, ordered by creation date descending.
     * 
     * @param userId The ID of the user.
     * @return A list of {@link Notification}s.
     */
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Marks a specific notification as read.
     * 
     * @param id The unique identifier of the notification.
     * @throws RuntimeException if the notification is not found.
     */
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Returns the count of unread notifications for a user.
     * 
     * @param userId The ID of the user.
     * @return Total count of unread notifications.
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Marks all unread notifications for a specific user as read.
     * 
     * @param userId The ID of the user.
     */
    public void readAllForUser(Long userId) {
        List<Notification> unread = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
