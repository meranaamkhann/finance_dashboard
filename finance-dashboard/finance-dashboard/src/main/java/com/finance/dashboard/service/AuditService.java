package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.AuditLogResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.model.AuditLog;
import com.finance.dashboard.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Append-only audit trail service.
 * Every write is done in a NEW transaction so a rollback in the caller
 * does not suppress the audit entry.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // ── Write ─────────────────────────────────────────────────────────────────

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actor, AuditAction action, String entityType, Long entityId,
                    String description, String previousState, String newState, String ip) {
        auditLogRepository.save(AuditLog.builder()
                .actorUsername(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .previousState(previousState)
                .newState(newState)
                .ipAddress(ip)
                .build());
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(auditLogRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getByActor(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(
                auditLogRepository.findAllByActorUsername(username, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getByEntity(String entityType, Long entityId,
                                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(
                auditLogRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getByDateRange(LocalDateTime from, LocalDateTime to,
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(
                auditLogRepository.findAllByCreatedAtBetween(from, to, pageable).map(this::toResponse));
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorUsername(log.getActorUsername())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .previousState(log.getPreviousState())
                .newState(log.getNewState())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
