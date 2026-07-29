package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long          id;
    private String        username;
    private String        email;
    private String        fullName;
    private Role          role;
    private boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
