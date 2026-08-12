package com.finance.dashboard.service;

import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.model.EmailVerificationToken;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.EmailVerificationTokenRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void sendVerification(User user) {
        if (user.isEmailVerified()) return;
        tokenRepo.deleteByUserId(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        tokenRepo.save(EmailVerificationToken.builder()
                .user(user).token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());
        emailService.sendEmailVerification(user.getEmail(), token, user.getUsername());
        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public void verify(String token) {
        EmailVerificationToken evt = tokenRepo.findByToken(token)
                .filter(EmailVerificationToken::isValid)
                .orElseThrow(() -> new BadRequestException(
                        "Verification link is invalid or expired. Request a new one."));
        User user = evt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        evt.setUsed(true);
        tokenRepo.save(evt);
        log.info("Email verified for user: {}", user.getUsername());
    }
}