package com.finance.dashboard.repository;

import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByActorUsername(String actorUsername, Pageable pageable);

    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findAllByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findAllByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
}
