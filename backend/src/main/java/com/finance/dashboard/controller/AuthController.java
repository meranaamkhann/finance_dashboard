package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.*;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.service.AuthService;
import com.finance.dashboard.util.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
@Tag(name="Authentication",description="Login, logout, token refresh")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login") @Operation(summary="Login — returns access + refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful",authService.login(req,IpUtils.resolveIp(http))));
    }
    @PostMapping("/refresh") @Operation(summary="Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed",authService.refresh(req)));
    }
}
