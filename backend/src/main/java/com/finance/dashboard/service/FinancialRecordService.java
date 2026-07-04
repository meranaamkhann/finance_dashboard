package com.finance.dashboard.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.*;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class FinancialRecordService {
    private final FinancialRecordRepository recordRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;
    private final BudgetAlertService budgetAlertService;
    private final ObjectMapper objectMapper;

    @Transactional
    @Caching(evict = { @CacheEvict(value="dashboardSummary",allEntries=true), @CacheEvict(value="healthScore",allEntries=true) })
    public FinancialRecordResponse create(CreateRecordRequest req, String ip) {
        User actor = securityUtils.getCurrentUser();
        FinancialRecord r = FinancialRecord.builder()
                .type(req.getType()).category(req.getCategory()).amount(req.getAmount()).date(req.getDate())
                .description(req.getDescription()!=null?req.getDescription().trim():null)
                .tags(normaliseTags(req.getTags())).createdBy(actor).build();
        recordRepository.save(r);
        auditService.log(AuditAction.RECORD_CREATED, actor.getUsername(), "FinancialRecord", r.getId(),
                null, json(r), ip, req.getType() + " ₹" + req.getAmount() + " [" + req.getCategory() + "]");
        if (req.getType() == TransactionType.EXPENSE)
            budgetAlertService.evaluate(actor, req.getCategory(), req.getDate());
        return toResponse(r);
    }

    @Transactional(readOnly=true)
    public PagedResponse<FinancialRecordResponse> getAll(TransactionType type, Category category,
            LocalDate from, LocalDate to, String keyword, String tags, Long createdById, Pageable pageable) {
        Specification<FinancialRecord> spec = FinancialRecordSpecification.filter(type, category, from, to, keyword, tags, createdById);
        return new PagedResponse<>(recordRepository.findAll(spec, pageable).map(this::toResponse));
    }

    @Transactional(readOnly=true)
    public FinancialRecordResponse getById(Long id) { return toResponse(findActive(id)); }

    @Transactional
    @Caching(evict = { @CacheEvict(value="dashboardSummary",allEntries=true), @CacheEvict(value="healthScore",allEntries=true) })
    public FinancialRecordResponse update(Long id, UpdateRecordRequest req, String ip) {
        FinancialRecord r = findActive(id); String before = json(r);
        if (req.getType()!=null) r.setType(req.getType());
        if (req.getCategory()!=null) r.setCategory(req.getCategory());
        if (req.getAmount()!=null) r.setAmount(req.getAmount());
        if (req.getDate()!=null) r.setDate(req.getDate());
        if (req.getDescription()!=null) r.setDescription(req.getDescription().trim());
        if (req.getTags()!=null) r.setTags(normaliseTags(req.getTags()));
        recordRepository.save(r);
        auditService.log(AuditAction.RECORD_UPDATED, securityUtils.getCurrentUsername(),
                "FinancialRecord", id, before, json(r), ip, "Updated record #" + id);
        return toResponse(r);
    }

    @Transactional
    @Caching(evict = { @CacheEvict(value="dashboardSummary",allEntries=true), @CacheEvict(value="healthScore",allEntries=true) })
    public void delete(Long id, String ip) {
        FinancialRecord r = findActive(id); String before = json(r);
        r.setDeleted(true); recordRepository.save(r);
        auditService.log(AuditAction.RECORD_DELETED, securityUtils.getCurrentUsername(),
                "FinancialRecord", id, before, null, ip, "Soft-deleted #" + id);
    }

    private FinancialRecord findActive(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("FinancialRecord", id));
    }
    private String normaliseTags(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Arrays.stream(raw.split(",")).map(String::trim).map(String::toLowerCase)
                .filter(t -> !t.isEmpty()).distinct().sorted().collect(Collectors.joining(","));
    }
    public FinancialRecordResponse toResponse(FinancialRecord r) {
        return FinancialRecordResponse.builder().id(r.getId()).type(r.getType()).category(r.getCategory())
                .amount(r.getAmount()).date(r.getDate()).description(r.getDescription()).tags(r.getTags())
                .autoGenerated(r.isAutoGenerated()).createdBy(r.getCreatedBy()!=null?r.getCreatedBy().getUsername():null)
                .createdAt(r.getCreatedAt()).updatedAt(r.getUpdatedAt()).build();
    }
    private String json(Object o) { try{return objectMapper.writeValueAsString(o);}catch(Exception e){return "{}";} }
}
