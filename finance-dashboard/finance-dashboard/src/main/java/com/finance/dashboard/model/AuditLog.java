package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit trail.  Every meaningful state-change in the system
 * (create, update, delete, login, export, budget alert) is written here.
 * Records are never updated or deleted — they are append-only.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_actor",      columnList = "actor_username"),
    @Index(name = "idx_audit_entity",     columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_timestamp",  columnList = "created_at")
})
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who performed the action. */
    @Column(nullable = false, length = 50)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    /** The entity type that was affected, e.g. "FinancialRecord", "User". */
    @Column(nullable = false, length = 100)
    private String entityType;

    /** The PK of the affected entity. Null for login/logout/export events. */
    private Long entityId;

    /** Human-readable summary of what changed. */
    @Column(length = 1000)
    private String description;

    /** JSON snapshot of the previous state (for UPDATE / DELETE). */
    @Column(columnDefinition = "TEXT")
    private String previousState;

    /** JSON snapshot of the new state (for CREATE / UPDATE). */
    @Column(columnDefinition = "TEXT")
    private String newState;

    /** Client IP address for traceability. */
    @Column(length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
