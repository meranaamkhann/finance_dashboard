package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.BudgetResponse;
import com.finance.dashboard.exception.*;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service @RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final FinancialRecordRepository recordRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional
    public BudgetResponse create(BudgetRequest req, String ip) {
        User user = securityUtils.getCurrentUser();
        if (budgetRepository.existsByUserIdAndCategoryAndActiveTrueAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
                user.getId(), req.getCategory(), req.getPeriodEnd(), req.getPeriodStart()))
            throw new BadRequestException("Active budget for " + req.getCategory() + " already overlaps this period");
        Budget b = Budget.builder().user(user).category(req.getCategory())
                .limitAmount(req.getLimitAmount()).periodStart(req.getPeriodStart()).periodEnd(req.getPeriodEnd()).build();
        budgetRepository.save(b);
        auditService.log(AuditAction.BUDGET_CREATED, user.getUsername(), "Budget", b.getId(), null, null, ip,
                "Created budget for " + req.getCategory() + ": ₹" + req.getLimitAmount());
        return toResponse(b, user.getId());
    }

    @Transactional(readOnly=true)
    public List<BudgetResponse> getMyBudgets() {
        Long uid = securityUtils.getCurrentUserId();
        return budgetRepository.findAllByUserIdAndActiveTrue(uid).stream().map(b -> toResponse(b, uid)).toList();
    }

    @Transactional(readOnly=true)
    public BudgetResponse getById(Long id) {
        Long uid = securityUtils.getCurrentUserId();
        return toResponse(budgetRepository.findByIdAndUserIdAndActiveTrue(id, uid)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id)), uid);
    }

    @Transactional
    public BudgetResponse update(Long id, BudgetRequest req, String ip) {
        Long uid = securityUtils.getCurrentUserId();
        Budget b = budgetRepository.findByIdAndUserIdAndActiveTrue(id, uid)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        b.setLimitAmount(req.getLimitAmount()); b.setPeriodStart(req.getPeriodStart());
        b.setPeriodEnd(req.getPeriodEnd()); b.setCategory(req.getCategory());
        budgetRepository.save(b);
        auditService.log(AuditAction.BUDGET_UPDATED, securityUtils.getCurrentUsername(), "Budget", id, null, null, ip, "Updated budget");
        return toResponse(b, uid);
    }

    @Transactional
    public void delete(Long id, String ip) {
        Long uid = securityUtils.getCurrentUserId();
        Budget b = budgetRepository.findByIdAndUserIdAndActiveTrue(id, uid)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        b.setActive(false); budgetRepository.save(b);
        auditService.log(AuditAction.BUDGET_DELETED, securityUtils.getCurrentUsername(), "Budget", id, null, null, ip, "Deactivated budget");
    }

    private BudgetResponse toResponse(Budget b, Long uid) {
        BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(uid, b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
        if (spent == null) spent = BigDecimal.ZERO;
        BigDecimal remaining = b.getLimitAmount().subtract(spent);
        double pct = spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
        String status = pct >= 100 ? "EXCEEDED" : pct >= 90 ? "CRITICAL" : pct >= 80 ? "WARNING" : "ON_TRACK";
        return BudgetResponse.builder().id(b.getId()).category(b.getCategory()).limitAmount(b.getLimitAmount())
                .spentAmount(spent).remainingAmount(remaining.max(BigDecimal.ZERO))
                .usagePercent(Math.min(pct, 100.0)).status(status)
                .periodStart(b.getPeriodStart()).periodEnd(b.getPeriodEnd()).active(b.isActive()).createdAt(b.getCreatedAt()).build();
    }
}
