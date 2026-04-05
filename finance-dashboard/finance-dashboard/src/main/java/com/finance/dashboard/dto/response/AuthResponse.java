package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AuthResponse {
    private String token;
    private String refreshHint;   // tells client when token expires (ISO datetime)
    private Long   userId;
    private String username;
    private String email;
    private String fullName;
    private Role   role;
}
