package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String ownerUsername;
    private String ownerFullName;
    private int memberCount;
    private int maxMembers;
    private List<WorkspaceMemberResponse> members;
    private LocalDateTime createdAt;
}