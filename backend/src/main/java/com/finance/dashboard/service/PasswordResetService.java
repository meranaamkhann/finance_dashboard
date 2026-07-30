package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.ForgotPasswordRequest;
import com.finance.dashboard.dto.request.ResetPasswordRequest;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.model.PasswordResetToken;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.PasswordResetTokenRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public void requestReset(ForgotPasswordRequest req) {
        userRepository.findByEmailAndDeletedFalse(req.getEmail().toLowerCase().trim())
                .ifPresent(user -> {
                    tokenRepository.invalidateAllForUser(user.getId());
                    String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
                    tokenRepository.save(PasswordResetToken.builder()
                            .user(user).token(token)
                            .expiresAt(LocalDateTime.now().plusMinutes(30)).build());
                    emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
                    auditService.log(AuditAction.PASSWORD_CHANGED, user.getUsername(),
                            "Password reset requested", null);
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new BadRequestException("Passwords do not match");

        PasswordResetToken prt = tokenRepository.findByToken(req.getToken())
                .filter(PasswordResetToken::isValid)
                .orElseThrow(() -> new BadRequestException("Reset token is invalid or expired"));

        User user = prt.getUser();
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword()))
            throw new BadRequestException("New password must differ from current password");

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetFailedAttempts();
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
        refreshTokenService.revokeAll(user.getId());

        emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());
        auditService.log(AuditAction.PASSWORD_CHANGED, user.getUsername(),
                "Password reset completed", null);
    }
}
