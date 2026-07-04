package com.finance.dashboard.service;
import com.finance.dashboard.dto.response.AuditLogResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.model.AuditLog;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class AuditQueryService {
    private final AuditLogRepository repo;
    @Transactional(readOnly=true) public PagedResponse<AuditLogResponse> getAll(Pageable p)      { return new PagedResponse<>(repo.findAllByOrderByCreatedAtDesc(p).map(this::toResponse)); }
    @Transactional(readOnly=true) public PagedResponse<AuditLogResponse> getByActor(String u,Pageable p) { return new PagedResponse<>(repo.findByActorUsernameOrderByCreatedAtDesc(u,p).map(this::toResponse)); }
    @Transactional(readOnly=true) public PagedResponse<AuditLogResponse> getByRange(LocalDateTime f,LocalDateTime t,Pageable p) { return new PagedResponse<>(repo.findByCreatedAtBetweenOrderByCreatedAtDesc(f,t,p).map(this::toResponse)); }
    @Transactional(readOnly=true) public PagedResponse<AuditLogResponse> getByAction(AuditAction a,Pageable p) { return new PagedResponse<>(repo.findByActionOrderByCreatedAtDesc(a,p).map(this::toResponse)); }
    private AuditLogResponse toResponse(AuditLog a) {
        return AuditLogResponse.builder().id(a.getId()).action(a.getAction()).actorUsername(a.getActorUsername())
                .entityType(a.getEntityType()).entityId(a.getEntityId()).ipAddress(a.getIpAddress())
                .detail(a.getDetail()).createdAt(a.getCreatedAt()).build();
    }
}
