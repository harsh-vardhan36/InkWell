package com.inkWell.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendHtmlEmail_Success() {
        emailService.sendHtmlEmail("test@example.com", "Subject", "<h1>Body</h1>");
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void buildVerificationOtpHtml_ContainsInfo() {
        String html = emailService.buildVerificationOtpHtml("John Doe", "123456");
        assertTrue(html.contains("John Doe"));
        assertTrue(html.contains("123456"));
        assertTrue(html.contains("Verify your email address"));
    }

    @Test
    void buildPasswordResetOtpHtml_ContainsInfo() {
        String html = emailService.buildPasswordResetOtpHtml("John Doe", "123456");
        assertTrue(html.contains("John Doe"));
        assertTrue(html.contains("123456"));
        assertTrue(html.contains("Password Reset Request"));
    }

    @Test
    void buildLoginAlertHtml_ContainsInfo() {
        String html = emailService.buildLoginAlertHtml("John Doe");
        assertTrue(html.contains("John Doe"));
        assertTrue(html.contains("New Login Detected"));
    }

    @Test
    void buildAuthorVerificationOtpHtml_ContainsInfo() {
        String html = emailService.buildAuthorVerificationOtpHtml("John Doe", "1234");
        assertTrue(html.contains("John Doe"));
        assertTrue(html.contains("1234"));
        assertTrue(html.contains("Author Verification"));
    }

    @Test
    void buildAccountDeactivationOtpHtml_ContainsInfo() {
        String html = emailService.buildAccountDeactivationOtpHtml("John Doe", "4321");
        assertTrue(html.contains("John Doe"));
        assertTrue(html.contains("4321"));
        assertTrue(html.contains("Deactivate Account Request"));
    }
}
