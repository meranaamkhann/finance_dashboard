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
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — Reset Your Password");
            String link = frontendUrl + "/reset-password?token=" + token;
            msg.setText(
                "Hi " + username + ",

" +
                "We received a request to reset your FinancePro password.

" +
                "Click the link below to reset it (valid for 30 minutes):
" +
                link + "

" +
                "If you did not request this, ignore this email. Your password will not change.

" +
                "— FinancePro Team"
            );
            mailSender.send(msg);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginNotification(String toEmail, String username, String ipAddress) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — New Login Detected");
            msg.setText(
                "Hi " + username + ",

" +
                "A new login was detected on your FinancePro account.

" +
                "IP Address: " + ipAddress + "
" +
                "Time: " + java.time.LocalDateTime.now() + "

" +
                "If this was you, no action is needed.
" +
                "If you did not log in, change your password immediately.

" +
                "— FinancePro Team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send login notification to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordChangedNotification(String toEmail, String username) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("FinancePro — Password Changed");
            msg.setText(
                "Hi " + username + ",

" +
                "Your FinancePro password was successfully changed.

" +
                "If you did not make this change, contact support immediately.

" +
                "— FinancePro Team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send password changed notification: {}", e.getMessage());
        }
    }
}
