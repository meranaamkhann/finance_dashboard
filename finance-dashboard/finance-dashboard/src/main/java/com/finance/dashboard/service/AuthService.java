package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import com.finance.dashboard.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils              jwtUtils;
    private final UserRepository        userRepository;
    private final AuditService          auditService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtUtils.generateToken(auth);
        LocalDateTime expiry = jwtUtils.getExpiryFromToken(token);

        // Reset failed attempts and update last-login timestamp
        userRepository.resetFailedAttemptsAndUpdateLogin(principal.getId());

        // Audit the login event
        auditService.log(principal.getUsername(), AuditAction.LOGIN,
                "User", principal.getId(), "Successful login", null, null, ipAddress);

        return AuthResponse.builder()
                .token(token)
                .refreshHint(expiry.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .userId(principal.getId())
                .username(principal.getUsername())
                .email(principal.getEmail())
                .fullName(principal.getFullName())
                .role(principal.getRole())
                .build();
    }
}
