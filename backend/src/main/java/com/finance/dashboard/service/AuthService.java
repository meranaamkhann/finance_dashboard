package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.LoginByEmailRequest;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.LogoutRequest;
import com.finance.dashboard.dto.request.RefreshTokenRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.exception.AccountLockedException;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.RefreshTokenRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import com.finance.dashboard.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest req, String ip) {
        User user = userRepository.findByUsernameAndDeletedFalse(req.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return doLogin(user, req.getPassword(), ip);
    }

    @Transactional
    public AuthResponse loginByEmail(LoginByEmailRequest req, String ip) {
        User user = userRepository.findByEmailAndDeletedFalse(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return doLogin(user, req.getPassword(), ip);
    }

    private AuthResponse doLogin(User user, String rawPassword, String ip) {
        if (user.isAccountLocked())
            throw new AccountLockedException("Account locked until " + user.getLockedUntil());
        if (!user.isActive())
            throw new DisabledException("Account is disabled. Contact support.");

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), rawPassword));
        } catch (BadCredentialsException ex) {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                log.warn("Account {} locked after {} failed attempts", user.getUsername(), MAX_ATTEMPTS);
            }
            userRepository.save(user);
            auditService.log(AuditAction.LOGIN_FAILURE, user.getUsername(),
                    "Failed attempt #" + user.getFailedLoginAttempts() + " from " + ip, ip);
            throw ex;
        }

        userRepository.resetFailedAttempts(user.getId());
        String role = "ROLE_" + user.getRole().name();
        String rawRefresh = jwtUtils.generateRefreshToken(user.getUsername());
        refreshTokenService.create(user, rawRefresh);

        auditService.log(AuditAction.LOGIN_SUCCESS, user.getUsername(), "Login from " + ip, ip);
        emailService.sendLoginNotification(user.getEmail(), user.getUsername(), ip);

        return buildResponse(user, jwtUtils.generateAccessToken(user.getUsername(), role), rawRefresh);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        if (!jwtUtils.validateToken(req.getRefreshToken()))
            throw new BadRequestException("Refresh token is invalid or expired");
        if (!"refresh".equals(jwtUtils.getTokenType(req.getRefreshToken())))
            throw new BadRequestException("Not a refresh token");

        refreshTokenService.findValid(req.getRefreshToken())
                .orElseThrow(() -> new BadRequestException("Refresh token has been revoked or expired"));

        String username = jwtUtils.getUsernameFromToken(req.getRefreshToken());
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive() || user.isAccountLocked())
            throw new BadRequestException("Account disabled or locked");

        refreshTokenService.revoke(req.getRefreshToken());
        String newRefresh = jwtUtils.generateRefreshToken(username);
        refreshTokenService.create(user, newRefresh);

        String role = "ROLE_" + user.getRole().name();
        auditService.log(AuditAction.TOKEN_REFRESH, username, "Token refreshed", null);
        return buildResponse(user, jwtUtils.generateAccessToken(username, role), newRefresh);
    }

    @Transactional
    public void logout(LogoutRequest req) {
        if (req.getRefreshToken() != null && !req.getRefreshToken().isBlank()) {
            refreshTokenService.revoke(req.getRefreshToken());
        }
    }

    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenService.revokeAll(user.getId());
        auditService.log(AuditAction.LOGOUT, username, "Logged out all devices", null);
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
