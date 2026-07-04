package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.RefreshTokenRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.exception.AccountLockedException;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import com.finance.dashboard.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j @Service @RequiredArgsConstructor
public class AuthService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Value("${app.jwt.expiration-ms:86400000}") private long jwtExpirationMs;

    @Transactional
    public AuthResponse login(LoginRequest req, String ip) {
        User user = userRepository.findByUsernameAndDeletedFalse(req.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (user.isAccountLocked()) throw new AccountLockedException("Account locked until " + user.getLockedUntil() + ". Too many failed attempts.");
        if (!user.isActive()) throw new DisabledException("Account is disabled");
        try {
            var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
            String role = ud.getAuthorities().iterator().next().getAuthority();
            userRepository.resetFailedAttempts(user.getId());
            auditService.log(AuditAction.LOGIN_SUCCESS, user.getUsername(), "Login from " + ip, ip);
            return AuthResponse.builder().accessToken(jwtUtils.generateAccessToken(ud.getUsername(), role))
                    .refreshToken(jwtUtils.generateRefreshToken(ud.getUsername())).tokenType("Bearer")
                    .expiresIn(jwtExpirationMs/1000).username(ud.getUsername()).fullName(user.getFullName()).role(user.getRole()).build();
        } catch (BadCredentialsException ex) {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                log.warn("Account {} locked after {} failed attempts", user.getUsername(), MAX_ATTEMPTS);
            }
            userRepository.save(user);
            auditService.log(AuditAction.LOGIN_FAILURE, user.getUsername(), "Failed attempt #" + user.getFailedLoginAttempts() + " from " + ip, ip);
            throw ex;
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        if (!jwtUtils.validateToken(req.getRefreshToken())) throw new BadRequestException("Invalid or expired refresh token");
        if (!"refresh".equals(jwtUtils.getTokenType(req.getRefreshToken()))) throw new BadRequestException("Not a refresh token");
        String username = jwtUtils.getUsernameFromToken(req.getRefreshToken());
        User user = userRepository.findByUsernameAndDeletedFalse(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive() || user.isAccountLocked()) throw new BadRequestException("Account disabled or locked");
        String role = "ROLE_" + user.getRole().name();
        auditService.log(AuditAction.TOKEN_REFRESH, username, "Token refreshed", null);
        return AuthResponse.builder().accessToken(jwtUtils.generateAccessToken(username, role))
                .refreshToken(jwtUtils.generateRefreshToken(username)).tokenType("Bearer")
                .expiresIn(jwtExpirationMs/1000).username(username).fullName(user.getFullName()).role(user.getRole()).build();
    }
}
