package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.request.ForgotPasswordRequest;
import com.finance.dashboard.dto.request.LoginByEmailRequest;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.LogoutRequest;
import com.finance.dashboard.dto.request.RefreshTokenRequest;
import com.finance.dashboard.dto.request.ResetPasswordRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.service.AuthService;
import com.finance.dashboard.service.EmailVerificationService;
import com.finance.dashboard.service.PasswordResetService;
import com.finance.dashboard.service.UserService;
import com.finance.dashboard.util.IpUtils;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final SecurityUtils securityUtils;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody CreateUserRequest req,
            HttpServletRequest http) {
        UserResponse created = userService.create(req, "SELF_REGISTER", IpUtils.resolveIp(http));
        User user = userRepository.findByUsernameAndDeletedFalse(req.getUsername().toLowerCase()).orElseThrow();emailVerificationService.sendVerification(user);
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(req.getUsername());
        loginReq.setPassword(req.getPassword());
        AuthResponse auth = authService.login(loginReq, IpUtils.resolveIp(http));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created! Please verify your email.", auth));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                authService.login(req, IpUtils.resolveIp(http))));
    }

    @PostMapping("/login/email")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> loginByEmail(
            @Valid @RequestBody LoginByEmailRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                authService.loginByEmail(req, IpUtils.resolveIp(http))));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed",
                authService.refresh(req)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest req) {
        authService.logout(req);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        authService.logoutAll(securityUtils.getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.ok("Logged out from all devices", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.requestReset(req);
        return ResponseEntity.ok(ApiResponse.ok(
                "If that email exists, a reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        passwordResetService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful. Please log in.", null));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address using token from email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        emailVerificationService.verify(token);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully. You can now log in.", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestBody @Valid ForgotPasswordRequest req) {
        userRepository.findByEmailAndDeletedFalse(req.getEmail().toLowerCase().trim())
                .ifPresent(emailVerificationService::sendVerification);
        return ResponseEntity.ok(ApiResponse.ok(
                "If that email exists and is unverified, a new link has been sent.", null));
    }
}
