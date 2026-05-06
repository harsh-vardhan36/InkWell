package com.inkWell.notification.consumer;

import com.inkWell.notification.config.RabbitConfig;
import com.inkWell.notification.dto.NotificationRequest;
import com.inkWell.notification.service.EmailService;
import com.inkWell.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final EmailService emailService;
    private final NotificationService notificationService;

    private static final String FULL_NAME = "fullName";
    private static final String OTP = "otp";

    public NotificationConsumer(EmailService emailService, NotificationService notificationService) {
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void consumeNotification(NotificationRequest request) {
        log.info("Received notification request for: {}", request.getTo());
        
        String htmlBody = "";
        switch (request.getType()) {
            case "VERIFICATION":
                htmlBody = emailService.buildVerificationOtpHtml(
                    request.getData().get(FULL_NAME),
                    request.getData().get(OTP)
                );
                break;
            case "PASSWORD_RESET":
                htmlBody = emailService.buildPasswordResetOtpHtml(
                    request.getData().get(FULL_NAME),
                    request.getData().get(OTP)
                );
                break;
            case "LOGIN_ALERT":
                htmlBody = emailService.buildLoginAlertHtml(
                    request.getData().get(FULL_NAME)
                );
                break;
            case "AUTHOR_VERIFICATION":
                htmlBody = emailService.buildAuthorVerificationOtpHtml(
                    request.getData().get(FULL_NAME),
                    request.getData().get(OTP)
                );
                break;
            case "ACCOUNT_DEACTIVATION":
                htmlBody = emailService.buildAccountDeactivationOtpHtml(
                    request.getData().get(FULL_NAME),
                    request.getData().get(OTP)
                );
                break;
            default:
                log.warn("Unknown notification type: {}", request.getType());
                return;
        }

        emailService.sendHtmlEmail(request.getTo(), request.getSubject(), htmlBody);

        // Save in-app notification if userId is present
        if (request.getUserId() != null) {
            try {
                com.inkWell.notification.domain.entity.Notification notification = new com.inkWell.notification.domain.entity.Notification();
                notification.setUserId(request.getUserId());
                notification.setMessage(request.getSubject());
                notification.setType(com.inkWell.notification.domain.enums.NotificationType.valueOf(request.getType()));
                notificationService.createNotification(notification);
            } catch (IllegalArgumentException e) {
                log.warn("Cannot save in-app notification for type '{}': not a valid NotificationType", request.getType());
            }
        }
    }
}
