package com.finance.dashboard.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@financepro.app}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token, String username) {
        try {
            String link = frontendUrl + "/reset-password?token=" + token;
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — Reset Your Password");
            msg.setText("Hi " + username + ",\n\n"
                + "Click the link below to reset your password (valid 30 minutes):\n"
                + link + "\n\n"
                + "If you did not request this, ignore this email.\n\n"
                + "— FinancePro Team");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginNotification(String toEmail, String username, String ip) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — New Login Detected");
            msg.setText("Hi " + username + ",\n\n"
                + "A new login was detected on your account.\n"
                + "IP: " + ip + "\n\n"
                + "If this was not you, change your password immediately.\n\n"
                + "— FinancePro Team");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send login notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordChangedNotification(String toEmail, String username) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — Password Changed");
            msg.setText("Hi " + username + ",\n\n"
                + "Your password was successfully changed.\n\n"
                + "If you did not do this, contact support immediately.\n\n"
                + "— FinancePro Team");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password changed email: {}", e.getMessage());
        }
    }
}