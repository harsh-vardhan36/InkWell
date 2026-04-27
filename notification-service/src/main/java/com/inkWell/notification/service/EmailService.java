package com.inkWell.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@inkwell.app", "InkWell");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public String buildVerificationOtpHtml(String fullName, String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Verify your InkWell Account</title></head>" +
            "<body style='margin:0;padding:0;background-color:#0f0f1a;font-family:\"Segoe UI\",Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#0f0f1a;padding:40px 20px;'>" +
            "  <tr><td align='center'>" +
            "    <table width='600' cellpadding='0' cellspacing='0' style='background:linear-gradient(145deg,#1a1a2e,#16213e);border-radius:16px;overflow:hidden;border:1px solid rgba(139,92,246,0.3);'>" +
            "      <tr><td style='background:linear-gradient(135deg,#6d28d9,#4f46e5);padding:40px 48px;text-align:center;'>" +
            "        <div style='font-size:32px;font-weight:800;color:#ffffff;letter-spacing:-1px;'>✍️ InkWell</div>" +
            "        <div style='color:rgba(255,255,255,0.75);font-size:14px;margin-top:6px;letter-spacing:2px;text-transform:uppercase;'>Where Stories Come Alive</div>" +
            "      </td></tr>" +
            "      <tr><td style='padding:48px;'>" +
            "        <h1 style='color:#e2e8f0;font-size:24px;font-weight:700;margin:0 0 12px;'>Verify your email address 🔐</h1>" +
            "        <p style='color:#94a3b8;font-size:16px;line-height:1.6;margin:0 0 32px;'>Hi <strong style='color:#c4b5fd;'>" + fullName + "</strong>, welcome to InkWell! You're just one step away from joining our community of writers and readers.</p>" +
            "        <div style='background:rgba(109,40,217,0.15);border:1px solid rgba(139,92,246,0.4);border-radius:12px;padding:32px;text-align:center;margin:0 0 32px;'>" +
            "          <p style='color:#94a3b8;font-size:13px;text-transform:uppercase;letter-spacing:2px;margin:0 0 16px;'>Your verification code</p>" +
            "          <div style='font-size:48px;font-weight:800;letter-spacing:16px;color:#c4b5fd;font-family:monospace;'>" + otp + "</div>" +
            "          <p style='color:#64748b;font-size:13px;margin:16px 0 0;'>⏱ Expires in <strong style='color:#f59e0b;'>5 minutes</strong></p>" +
            "        </div>" +
            "        <p style='color:#64748b;font-size:14px;line-height:1.6;margin:0 0 16px;'>Enter this code in the InkWell app to activate your account.</p>" +
            "      </td></tr>" +
            "    </table>" +
            "  </td></tr>" +
            "</table>" +
            "</body></html>";
    }

    public String buildPasswordResetOtpHtml(String fullName, String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Reset your InkWell Password</title></head>" +
            "<body style='margin:0;padding:0;background-color:#0f0f1a;font-family:\"Segoe UI\",Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#0f0f1a;padding:40px 20px;'>" +
            "  <tr><td align='center'>" +
            "    <table width='600' cellpadding='0' cellspacing='0' style='background:linear-gradient(145deg,#1a1a2e,#16213e);border-radius:16px;overflow:hidden;border:1px solid rgba(239,68,68,0.3);'>" +
            "      <tr><td style='background:linear-gradient(135deg,#b91c1c,#7c3aed);padding:40px 48px;text-align:center;'>" +
            "        <div style='font-size:32px;font-weight:800;color:#ffffff;letter-spacing:-1px;'>✍️ InkWell</div>" +
            "      </td></tr>" +
            "      <tr><td style='padding:48px;'>" +
            "        <h1 style='color:#e2e8f0;font-size:24px;font-weight:700;margin:0 0 12px;'>Password Reset Request 🔑</h1>" +
            "        <p style='color:#94a3b8;font-size:16px;line-height:1.6;margin:0 0 32px;'>Hi <strong style='color:#fca5a5;'>" + fullName + "</strong>, use the code below to reset your password.</p>" +
            "        <div style='background:rgba(185,28,28,0.1);border:1px solid rgba(239,68,68,0.35);border-radius:12px;padding:32px;text-align:center;margin:0 0 32px;'>" +
            "          <div style='font-size:48px;font-weight:800;letter-spacing:16px;color:#fca5a5;font-family:monospace;'>" + otp + "</div>" +
            "        </div>" +
            "      </td></tr>" +
            "    </table>" +
            "  </td></tr>" +
            "</table>" +
            "</body></html>";
    }

    public String buildLoginAlertHtml(String fullName) {
        return "Hi " + fullName + ", a new login was detected on your InkWell account.";
    }

    public String buildAuthorVerificationOtpHtml(String fullName, String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Become an Author at InkWell</title></head>" +
            "<body style='margin:0;padding:0;background-color:#0f0f1a;font-family:\"Segoe UI\",Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#0f0f1a;padding:40px 20px;'>" +
            "  <tr><td align='center'>" +
            "    <table width='600' cellpadding='0' cellspacing='0' style='background:linear-gradient(145deg,#1a1a2e,#16213e);border-radius:16px;overflow:hidden;border:1px solid rgba(16,185,129,0.3);'>" +
            "      <tr><td style='background:linear-gradient(135deg,#059669,#0d9488);padding:40px 48px;text-align:center;'>" +
            "        <div style='font-size:32px;font-weight:800;color:#ffffff;letter-spacing:-1px;'>✍️ InkWell</div>" +
            "        <div style='color:rgba(255,255,255,0.75);font-size:14px;margin-top:6px;letter-spacing:2px;text-transform:uppercase;'>Author Verification</div>" +
            "      </td></tr>" +
            "      <tr><td style='padding:48px;'>" +
            "        <h1 style='color:#e2e8f0;font-size:24px;font-weight:700;margin:0 0 12px;'>Welcome to the Author Program 🚀</h1>" +
            "        <p style='color:#94a3b8;font-size:16px;line-height:1.6;margin:0 0 32px;'>Hi <strong style='color:#6ee7b7;'>" + fullName + "</strong>, you're one step away from becoming an InkWell Author! Use the code below to verify your identity and start publishing.</p>" +
            "        <div style='background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.35);border-radius:12px;padding:32px;text-align:center;margin:0 0 32px;'>" +
            "          <p style='color:#94a3b8;font-size:13px;text-transform:uppercase;letter-spacing:2px;margin:0 0 16px;'>Your 4-digit verification code</p>" +
            "          <div style='font-size:56px;font-weight:800;letter-spacing:20px;color:#6ee7b7;font-family:monospace;'>" + otp + "</div>" +
            "          <p style='color:#64748b;font-size:13px;margin:16px 0 0;'>⏱ Expires in <strong style='color:#f59e0b;'>10 minutes</strong></p>" +
            "        </div>" +
            "        <p style='color:#64748b;font-size:14px;line-height:1.6;margin:0 0 16px;'>Enter this code in the InkWell app to complete your author registration.</p>" +
            "        <p style='color:#475569;font-size:12px;margin:0;'>If you did not request this, please ignore this email.</p>" +
            "      </td></tr>" +
            "    </table>" +
            "  </td></tr>" +
            "</table>" +
            "</body></html>";
    }

    public String buildAccountDeactivationOtpHtml(String fullName, String otp) {
        return "<!DOCTYPE html>" +
            "<html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<title>Deactivate your InkWell Account</title></head>" +
            "<body style='margin:0;padding:0;background-color:#0f0f1a;font-family:\"Segoe UI\",Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#0f0f1a;padding:40px 20px;'>" +
            "  <tr><td align='center'>" +
            "    <table width='600' cellpadding='0' cellspacing='0' style='background:linear-gradient(145deg,#1a1a2e,#16213e);border-radius:16px;overflow:hidden;border:1px solid rgba(239,68,68,0.3);'>" +
            "      <tr><td style='background:linear-gradient(135deg,#b91c1c,#7c3aed);padding:40px 48px;text-align:center;'>" +
            "        <div style='font-size:32px;font-weight:800;color:#ffffff;letter-spacing:-1px;'>✍️ InkWell</div>" +
            "        <div style='color:rgba(255,255,255,0.75);font-size:14px;margin-top:6px;letter-spacing:2px;text-transform:uppercase;'>Security Verification</div>" +
            "      </td></tr>" +
            "      <tr><td style='padding:48px;'>" +
            "        <h1 style='color:#e2e8f0;font-size:24px;font-weight:700;margin:0 0 12px;'>Deactivate Account Request ⚠️</h1>" +
            "        <p style='color:#94a3b8;font-size:16px;line-height:1.6;margin:0 0 32px;'>Hi <strong style='color:#fca5a5;'>" + fullName + "</strong>, we received a request to deactivate your InkWell account. This action is permanent and will delete all your data.</p>" +
            "        <div style='background:rgba(185,28,28,0.1);border:1px solid rgba(239,68,68,0.35);border-radius:12px;padding:32px;text-align:center;margin:0 0 32px;'>" +
            "          <p style='color:#94a3b8;font-size:13px;text-transform:uppercase;letter-spacing:2px;margin:0 0 16px;'>Your deactivation code</p>" +
            "          <div style='font-size:56px;font-weight:800;letter-spacing:20px;color:#fca5a5;font-family:monospace;'>" + otp + "</div>" +
            "          <p style='color:#64748b;font-size:13px;margin:16px 0 0;'>⏱ Expires in <strong style='color:#f59e0b;'>10 minutes</strong></p>" +
            "        </div>" +
            "        <p style='color:#64748b;font-size:14px;line-height:1.6;margin:0 0 16px;'>Enter this code in the InkWell app to confirm account deactivation.</p>" +
            "        <p style='color:#475569;font-size:12px;margin:0;'>If you did not request this, please change your password immediately.</p>" +
            "      </td></tr>" +
            "    </table>" +
            "  </td></tr>" +
            "</table>" +
            "</body></html>";
    }
}
