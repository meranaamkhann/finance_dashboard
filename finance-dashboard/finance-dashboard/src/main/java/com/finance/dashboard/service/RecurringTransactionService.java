package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.RecurringTransactionRequest;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.RecurringTransactionResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.RecurringUtils;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository ruleRepo;
    private final FinancialRecordRepository      recordRepo;
    private final UserRepository                 userRepo;
    private final NotificationService            notificationService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest req) {
        User owner = currentUser();
        RecurringTransaction saved = ruleRepo.save(RecurringTransaction.builder()
                .owner(owner).name(req.getName()).amount(req.getAmount())
                .type(req.getType()).category(req.getCategory())
                .frequency(req.getFrequency()).startDate(req.getStartDate())
                .endDate(req.getEndDate()).description(req.getDescription())
                .active(true).build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RecurringTransactionResponse> getMy(int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PagedResponse.from(
                ruleRepo.findAllByOwnerIdAndActiveTrue(SecurityUtils.currentUserId(), p)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse getById(Long id) {
        return toResponse(findOwnedOrThrow(id));
    }

    @Transactional
    public RecurringTransactionResponse update(Long id, RecurringTransactionRequest req) {
        RecurringTransaction rule = findOwnedOrThrow(id);
        rule.setName(req.getName());
        rule.setAmount(req.getAmount());
        rule.setType(req.getType());
        rule.setCategory(req.getCategory());
        rule.setFrequency(req.getFrequency());
        rule.setStartDate(req.getStartDate());
        rule.setEndDate(req.getEndDate());
        rule.setDescription(req.getDescription());
        return toResponse(ruleRepo.save(rule));
    }

    @Transactional
    public void deactivate(Long id) {
        RecurringTransaction rule = findOwnedOrThrow(id);
        rule.setActive(false);
        ruleRepo.save(rule);
    }

    // ── Scheduler — runs daily at 01:00 ──────────────────────────────────────

    @Scheduled(cron = "${app.scheduler.recurring-cron}")
    @Transactional
    public void executeAllDueRules() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = ruleRepo.findAllDueForExecution(today);
        log.info("Recurring scheduler fired — {} rule(s) due for {}", due.size(), today);

        for (RecurringTransaction rule : due) {
            if (!RecurringUtils.isDueToday(rule, today)) continue;
            try {
                FinancialRecord record = FinancialRecord.builder()
                        .amount(rule.getAmount()).type(rule.getType())
                        .category(rule.getCategory()).date(today)
                        .description("[Auto] " + rule.getName())
                        .createdBy(rule.getOwner()).recurringRule(rule)
                        .deleted(false).build();
                recordRepo.save(record);
                rule.setLastExecutedDate(today);
                ruleRepo.save(rule);

                notificationService.send(rule.getOwner().getId(),
                        "✅ Recurring transaction posted",
                        "Auto-posted: %s — ₹%s (%s)".formatted(
                                rule.getName(), rule.getAmount(), rule.getCategory()),
                        "RECURRING_EXECUTED");

                log.info("Executed recurring rule id={} name='{}' for user={}",
                        rule.getId(), rule.getName(), rule.getOwner().getUsername());
            } catch (Exception ex) {
                log.error("Failed to execute recurring rule id={}: {}", rule.getId(), ex.getMessage());
            }
        }
    }

    // ── Mapper & helpers ──────────────────────────────────────────────────────

    private RecurringTransactionResponse toResponse(RecurringTransaction r) {
        return RecurringTransactionResponse.builder()
                .id(r.getId()).name(r.getName()).amount(r.getAmount())
                .type(r.getType()).category(r.getCategory())
                .frequency(r.getFrequency()).startDate(r.getStartDate())
                .endDate(r.getEndDate()).lastExecutedDate(r.getLastExecutedDate())
                .nextExecutionDate(RecurringUtils.nextExecutionDate(r))
                .description(r.getDescription()).active(r.isActive())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private RecurringTransaction findOwnedOrThrow(Long id) {
        return ruleRepo.findByIdAndOwnerId(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
    }

    private User currentUser() {
        return userRepo.findByUsername(SecurityUtils.currentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
