package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.ForgotPasswordRequest;
import com.finance.dashboard.dto.request.LoginByEmailRequest;
import com.finance.dashboard.dto.request.LoginRequest;
import com.finance.dashboard.dto.request.LogoutRequest;
import com.finance.dashboard.dto.request.RefreshTokenRequest;
import com.finance.dashboard.dto.request.ResetPasswordRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.AuthResponse;
import com.finance.dashboard.service.AuthService;
import com.finance.dashboard.service.PasswordResetService;
import com.finance.dashboard.util.IpUtils;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
