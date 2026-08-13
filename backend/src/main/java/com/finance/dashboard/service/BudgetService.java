package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.BudgetResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
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
    private final WorkspaceService workspaceService;

@Transactional
public BudgetResponse create(BudgetRequest req, String ip) {

    User user = securityUtils.getCurrentUser();

    if (user == null) {
        throw new BadRequestException("Authenticated user not found");
    }

    if (req.getCategory() == null) {
        throw new BadRequestException("Category is required");
    }

    if (req.getLimitAmount() == null ||
            req.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException(
                "Budget limit must be greater than zero"
        );
    }

    if (req.getPeriodStart() == null || req.getPeriodEnd() == null) {
        throw new BadRequestException(
                "Budget start and end dates are required"
        );
    }

    if (req.getPeriodEnd().isBefore(req.getPeriodStart())) {
        throw new BadRequestException(
                "Budget end date cannot be before start date"
        );
    }

    Long workspaceId = workspaceService.getMyWorkspaceId();

    if (workspaceId == null) {
        throw new BadRequestException(
                "No workspace found for your account"
        );
    }

    boolean overlapping =
            budgetRepository
                    .existsByUserIdAndCategoryAndActiveTrueAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(
                            user.getId(),
                            req.getCategory(),
                            req.getPeriodEnd(),
                            req.getPeriodStart()
                    );

    if (overlapping) {
        throw new BadRequestException(
                "Active budget for "
                        + req.getCategory()
                        + " overlaps this period"
        );
    }

    Budget budget =
            Budget.builder()
                    .user(user)
                    .category(req.getCategory())
                    .limitAmount(req.getLimitAmount())
                    .periodStart(req.getPeriodStart())
                    .periodEnd(req.getPeriodEnd())
                    .workspaceId(workspaceId)
                    .build();

    budget = budgetRepository.save(budget);

    auditService.log(
            AuditAction.BUDGET_CREATED,
            user.getUsername(),
            "Budget",
            budget.getId(),
            null,
            null,
            ip,
            "Created for " + req.getCategory()
    );

    return toResponse(budget, user.getId());
}

    @Transactional(readOnly = true)
    public List<BudgetResponse> getMyBudgets() {
        Long uid = securityUtils.getCurrentUserId();
        return budgetRepository.findAllByUserIdAndActiveTrue(uid)
                .stream().map(b -> toResponse(b, uid)).toList();
    }

    @Transactional(readOnly = true)
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
        b.setLimitAmount(req.getLimitAmount());
        b.setPeriodStart(req.getPeriodStart());
        b.setPeriodEnd(req.getPeriodEnd());
        b.setCategory(req.getCategory());
        budgetRepository.save(b);
        auditService.log(AuditAction.BUDGET_UPDATED, securityUtils.getCurrentUsername(),
                "Budget", id, null, null, ip, "Updated");
        return toResponse(b, uid);
    }

    @Transactional
    public void delete(Long id, String ip) {
        Long uid = securityUtils.getCurrentUserId();
        Budget b = budgetRepository.findByIdAndUserIdAndActiveTrue(id, uid)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        b.setActive(false); budgetRepository.save(b);
        auditService.log(AuditAction.BUDGET_DELETED, securityUtils.getCurrentUsername(),
                "Budget", id, null, null, ip, "Deactivated");
    }

    private BudgetResponse toResponse(Budget b, Long uid) {
        BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(
                uid, b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
        if (spent == null) spent = BigDecimal.ZERO;
        BigDecimal remaining = b.getLimitAmount().subtract(spent);
        double pct = 0.0;

        if (b.getLimitAmount() != null &&
                b.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {

        pct = spent
                .divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        String status = pct >= 100 ? "EXCEEDED" : pct >= 90 ? "CRITICAL"
                      : pct >= 80  ? "WARNING"  : "ON_TRACK";
        return BudgetResponse.builder().id(b.getId()).category(b.getCategory())
                .limitAmount(b.getLimitAmount()).spentAmount(spent)
                .remainingAmount(remaining.max(BigDecimal.ZERO))
                .usagePercent(Math.min(pct, 100.0)).status(status)
                .periodStart(b.getPeriodStart()).periodEnd(b.getPeriodEnd())
                .active(b.isActive()).createdAt(b.getCreatedAt()).build();
    }
}
