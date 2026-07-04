package com.finance.dashboard.model;
import com.finance.dashboard.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actor_username"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40) private AuditAction action;
    @Column(nullable = false, length = 50) private String actorUsername;
    @Column(length = 60) private String entityType;
    private Long entityId;
    @Column(columnDefinition = "TEXT") private String beforeState;
    @Column(columnDefinition = "TEXT") private String afterState;
    @Column(length = 60) private String ipAddress;
    @Column(length = 500) private String detail;
    @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
}
