package com.finance.dashboard.service;
import com.finance.dashboard.model.AuditLog;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j @Service @RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String actor, String entityType, Long entityId,
                    String before, String after, String ip, String detail) {
        try {
            repo.save(AuditLog.builder().action(action).actorUsername(actor!=null?actor:"SYSTEM")
                    .entityType(entityType).entityId(entityId).beforeState(before).afterState(after)
                    .ipAddress(ip).detail(detail).build());
        } catch (Exception e) { log.error("Audit write failed [{}]: {}", action, e.getMessage()); }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String actor, String detail, String ip) {
        log(action, actor, null, null, null, null, ip, detail);
    }
}
