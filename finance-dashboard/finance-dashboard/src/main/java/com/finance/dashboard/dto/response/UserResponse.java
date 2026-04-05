package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder
public class UserResponse {
    private Long          id;
    private String        username;
    private String        email;
    private String        fullName;
    private Role          role;
    private boolean       active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
