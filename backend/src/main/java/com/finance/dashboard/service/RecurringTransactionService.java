package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.RecurringTransactionRequest;
import com.finance.dashboard.dto.response.RecurringTransactionResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.RecurringTransaction;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.RecurringTransactionRepository;
import com.finance.dashboard.util.RecurringUtils;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class RecurringTransactionService {
    private final RecurringTransactionRepository repo;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest req, String ip) {
        User user = securityUtils.getCurrentUser();
        RecurringTransaction rt = RecurringTransaction.builder().user(user).name(req.getName())
                .type(req.getType()).category(req.getCategory()).amount(req.getAmount())
                .frequency(req.getFrequency()).startDate(req.getStartDate()).endDate(req.getEndDate())
                .nextExecutionDate(RecurringUtils.initialNextDate(req.getStartDate(), req.getFrequency())).build();
        repo.save(rt);
        auditService.log(AuditAction.RECURRING_CREATED, user.getUsername(), "RecurringTransaction", rt.getId(),
                null, null, ip, "Created: " + rt.getName());
        return toResponse(rt);
    }

    @Transactional(readOnly=true)
    public List<RecurringTransactionResponse> getMyRecurring() {
        return repo.findAllByUserIdAndActiveTrue(securityUtils.getCurrentUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly=true)
    public RecurringTransactionResponse getById(Long id) {
        return toResponse(repo.findByIdAndUserIdAndActiveTrue(id, securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id)));
    }

    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest req, String ip) {
        RecurringTransaction rt = repo.findByIdAndUserIdAndActiveTrue(id, securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        rt.setName(req.getName()); rt.setType(req.getType()); rt.setCategory(req.getCategory());
        rt.setAmount(req.getAmount()); rt.setFrequency(req.getFrequency());
        rt.setStartDate(req.getStartDate()); rt.setEndDate(req.getEndDate());
        rt.setNextExecutionDate(RecurringUtils.initialNextDate(req.getStartDate(), req.getFrequency()));
        repo.save(rt);
        auditService.log(AuditAction.RECURRING_UPDATED, securityUtils.getCurrentUsername(),
                "RecurringTransaction", id, null, null, ip, "Updated: " + rt.getName());
        return toResponse(rt);
    }

    @Transactional
    public void delete(Long id, String ip) {
        RecurringTransaction rt = repo.findByIdAndUserIdAndActiveTrue(id, securityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        rt.setActive(false); repo.save(rt);
        auditService.log(AuditAction.RECURRING_DELETED, securityUtils.getCurrentUsername(),
                "RecurringTransaction", id, null, null, ip, "Deactivated: " + rt.getName());
    }

    public RecurringTransactionResponse toResponse(RecurringTransaction rt) {
        return RecurringTransactionResponse.builder().id(rt.getId()).name(rt.getName()).type(rt.getType())
                .category(rt.getCategory()).amount(rt.getAmount()).frequency(rt.getFrequency())
                .startDate(rt.getStartDate()).endDate(rt.getEndDate())
                .nextExecutionDate(rt.getNextExecutionDate()).lastExecutedDate(rt.getLastExecutedDate())
                .active(rt.isActive()).createdAt(rt.getCreatedAt()).build();
    }
}
