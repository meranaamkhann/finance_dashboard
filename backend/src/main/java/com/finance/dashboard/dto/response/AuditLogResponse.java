package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.AuditAction;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private String actorUsername, entityType, ipAddress, detail;
    private Long entityId;
    private LocalDateTime createdAt;
}
