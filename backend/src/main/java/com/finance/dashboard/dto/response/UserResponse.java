package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Role;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder
public class UserResponse {
    private Long id;
    private String username, email, fullName;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt, updatedAt;
}
