package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.AuditAction;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class AuditLogResponse {
    private Long          id;
    private AuditAction   action;
    private String        actorUsername;
    private String        entityType;
    private Long          entityId;
    private String        ipAddress;
    private String        detail;
    private LocalDateTime createdAt;
}
