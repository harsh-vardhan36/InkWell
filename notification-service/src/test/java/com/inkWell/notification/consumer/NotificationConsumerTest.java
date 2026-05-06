package com.inkWell.notification.consumer;

import com.inkWell.notification.dto.NotificationRequest;
import com.inkWell.notification.service.EmailService;
import com.inkWell.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    void consume_Verification() {
        NotificationRequest request = NotificationRequest.builder()
                .to("test@ex.com")
                .subject("Verify")
                .type("VERIFICATION")
                .data(Map.of("fullName", "Test", "otp", "1234"))
                .build();

        when(emailService.buildVerificationOtpHtml(anyString(), anyString())).thenReturn("<html></html>");

        consumer.consumeNotification(request);

        verify(emailService).sendHtmlEmail(eq("test@ex.com"), eq("Verify"), anyString());
    }

    @Test
    void consume_PasswordReset() {
        NotificationRequest request = NotificationRequest.builder()
                .to("test@ex.com")
                .type("PASSWORD_RESET")
                .data(Map.of("fullName", "Test", "otp", "1234"))
                .build();

        when(emailService.buildPasswordResetOtpHtml(anyString(), anyString())).thenReturn("<html></html>");

        consumer.consumeNotification(request);

        verify(emailService).sendHtmlEmail(anyString(), any(), anyString());
    }

    @Test
    void consume_LoginAlert() {
        NotificationRequest request = NotificationRequest.builder()
                .to("test@ex.com")
                .type("LOGIN_ALERT")
                .data(Map.of("fullName", "Test"))
                .build();

        when(emailService.buildLoginAlertHtml(anyString())).thenReturn("<html></html>");

        consumer.consumeNotification(request);

        verify(emailService).sendHtmlEmail(anyString(), any(), anyString());
    }

    @Test
    void consume_UnknownType_LogsWarning() {
        NotificationRequest request = NotificationRequest.builder()
                .to("test@ex.com")
                .type("UNKNOWN")
                .build();

        consumer.consumeNotification(request);

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    void consume_SaveInApp_Success() {
        NotificationRequest request = NotificationRequest.builder()
                .to("test@ex.com")
                .type("VERIFICATION")
                .userId(1L)
                .data(Map.of("fullName", "Test", "otp", "1234"))
                .build();

        when(emailService.buildVerificationOtpHtml(anyString(), anyString())).thenReturn("<html></html>");

        consumer.consumeNotification(request);

        verify(notificationService).createNotification(any());
    }
}
