package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Role;
import lombok.*;
@Data @Builder
public class AuthResponse {
    private String accessToken, refreshToken, tokenType;
    private long expiresIn;
    private String username, fullName;
    private Role role;
}
