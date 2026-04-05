package com.finance.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.config.CacheConfig;
import com.finance.dashboard.dto.request.CreateRecordRequest;
import com.finance.dashboard.dto.request.UpdateRecordRequest;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepo;
    private final UserRepository            userRepo;
    private final AuditService              auditService;
    private final BudgetAlertService        budgetAlertService;
    private final ObjectMapper              objectMapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_DASHBOARD,    allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_HEALTH_SCORE, allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_CATEGORY,     allEntries = true)
    })
    public FinancialRecordResponse create(CreateRecordRequest req, String ip) {
        User actor = currentUser();
        FinancialRecord saved = recordRepo.save(FinancialRecord.builder()
                .amount(req.getAmount()).type(req.getType())
                .category(req.getCategory()).date(req.getDate())
                .description(req.getDescription()).tags(req.getTags())
                .createdBy(actor).deleted(false).build());

        auditService.log(actor.getUsername(), AuditAction.CREATE,
                "FinancialRecord", saved.getId(),
                "Created %s of %s in %s".formatted(req.getType(), req.getAmount(), req.getCategory()),
                null, toJson(toResponse(saved)), ip);

        // Trigger budget alert evaluation after new expense
        if (req.getType() == TransactionType.EXPENSE) {
            budgetAlertService.evaluate(actor.getId(), req.getCategory(), LocalDate.now());
        }

        return toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordResponse> getAll(
            TransactionType type, Category category,
            LocalDate dateFrom, LocalDate dateTo,
            String keyword, String tags, Long createdById,
            int page, int size, String sortBy, String dir) {

        Sort sort = dir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Specification<FinancialRecord> spec = FinancialRecordSpecification
                .buildFilter(type, category, dateFrom, dateTo, keyword, tags, createdById);
        return PagedResponse.from(
                recordRepo.findAll(spec, PageRequest.of(page, size, sort)).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getById(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_DASHBOARD,    allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_HEALTH_SCORE, allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_CATEGORY,     allEntries = true)
    })
    public FinancialRecordResponse update(Long id, UpdateRecordRequest req, String ip) {
        FinancialRecord rec = findActiveOrThrow(id);
        String before = toJson(toResponse(rec));

        if (req.getAmount()      != null) rec.setAmount(req.getAmount());
        if (req.getType()        != null) rec.setType(req.getType());
        if (req.getCategory()    != null) rec.setCategory(req.getCategory());
        if (req.getDate()        != null) rec.setDate(req.getDate());
        if (req.getDescription() != null) rec.setDescription(req.getDescription());
        if (req.getTags()        != null) rec.setTags(req.getTags());

        FinancialRecord saved = recordRepo.save(rec);
        auditService.log(SecurityUtils.currentUsername(), AuditAction.UPDATE,
                "FinancialRecord", id, "Updated record #" + id,
                before, toJson(toResponse(saved)), ip);

        return toResponse(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_DASHBOARD,    allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_HEALTH_SCORE, allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_CATEGORY,     allEntries = true)
    })
    public void delete(Long id, String ip) {
        FinancialRecord rec = findActiveOrThrow(id);
        rec.setDeleted(true);
        recordRepo.save(rec);
        auditService.log(SecurityUtils.currentUsername(), AuditAction.DELETE,
                "FinancialRecord", id, "Soft-deleted record #" + id, null, null, ip);
    }

    // ── Mapper & helpers ──────────────────────────────────────────────────────

    public FinancialRecordResponse toResponse(FinancialRecord r) {
        return FinancialRecordResponse.builder()
                .id(r.getId()).amount(r.getAmount()).type(r.getType())
                .category(r.getCategory()).date(r.getDate())
                .description(r.getDescription()).tags(r.getTags())
                .createdByUsername(r.getCreatedBy().getUsername())
                .recurringRuleId(r.getRecurringRule() != null ? r.getRecurringRule().getId() : null)
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt())
                .build();
    }

    private FinancialRecord findActiveOrThrow(Long id) {
        return recordRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
    }

    private User currentUser() {
        return userRepo.findByUsername(SecurityUtils.currentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
