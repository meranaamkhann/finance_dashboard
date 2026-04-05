package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.AuditAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder
public class AuditLogResponse {
    private Long          id;
    private String        actorUsername;
    private AuditAction   action;
    private String        entityType;
    private Long          entityId;
    private String        description;
    private String        previousState;
    private String        newState;
    private String        ipAddress;
    private LocalDateTime createdAt;
}
