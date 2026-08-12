package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.enums.WorkspaceMemberRole;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class WorkspaceMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String avatarUrl;
    private WorkspaceMemberRole role;
    private LocalDateTime joinedAt;
}