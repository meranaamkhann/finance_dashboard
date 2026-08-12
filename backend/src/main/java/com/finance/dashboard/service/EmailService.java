package com.finance.dashboard.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@financepro.app}")
    private String from;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendPasswordReset(String toEmail, String token, String username) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username",  username);
            ctx.setVariable("resetLink", frontendUrl + "/reset-password?token=" + token);
            sendHtml(toEmail, "FinancePro — Reset Your Password", "emails/password-reset", ctx);
        } catch (Exception e) {
            log.error("sendPasswordReset failed for {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendEmailVerification(String toEmail, String token, String username) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username", username);
            ctx.setVariable("verifyLink", frontendUrl + "/verify-email?token=" + token);
            sendHtml(toEmail, "FinancePro — Verify Your Email", "emails/email-verification", ctx);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginAlert(String toEmail, String username, String ip) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username",           username);
            ctx.setVariable("ipAddress",          ip != null ? ip : "unknown");
            ctx.setVariable("loginTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z")));
            ctx.setVariable("changePasswordLink", frontendUrl + "/forgot-password");
            sendHtml(toEmail, "FinancePro — New Login Detected", "emails/login-alert", ctx);
        } catch (Exception e) {
            log.error("sendLoginAlert failed for {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginNotification(String toEmail, String username, String ip) {
        sendLoginAlert(toEmail, username, ip);
    }

    @Async
    public void sendPasswordChanged(String toEmail, String username) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username", username);
            sendHtml(toEmail, "FinancePro — Password Changed", "emails/password-changed", ctx);
        } catch (Exception e) {
            log.error("sendPasswordChanged failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendPasswordChangedNotification(String toEmail, String username) {
        sendPasswordChanged(toEmail, username);
    }

    @Async
    public void sendPaymentSuccess(String toEmail, String username, String invoiceNumber,
                                   String planName, String billingCycle,
                                   String validUntil, String amount) {
        try {
            Context ctx = new Context();
            ctx.setVariable("username",      username);
            ctx.setVariable("invoiceNumber", invoiceNumber);
            ctx.setVariable("planName",      planName);
            ctx.setVariable("billingCycle",  billingCycle);
            ctx.setVariable("validUntil",    validUntil);
            ctx.setVariable("amount",        "Rs." + amount);
            sendHtml(toEmail, "FinancePro — Payment Successful", "emails/payment-success", ctx);
        } catch (Exception e) {
            log.error("sendPaymentSuccess failed: {}", e.getMessage());
        }
    }

    private void sendHtml(String to, String subject, String template, Context ctx) throws Exception {
        String html = templateEngine.process(template, ctx);
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(msg);
        log.info("Email sent [{}] to {}", subject, to);
    }
}
