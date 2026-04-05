package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.dto.response.BudgetResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BudgetService {

    @Value("${app.budget.warning-threshold}")  private int warningThreshold;
    @Value("${app.budget.critical-threshold}") private int criticalThreshold;

    private final BudgetRepository          budgetRepo;
    private final FinancialRecordRepository recordRepo;
    private final UserRepository            userRepo;

    @Transactional
    public BudgetResponse create(BudgetRequest req) {
        if (req.getPeriodEnd().isBefore(req.getPeriodStart()))
            throw new BadRequestException("Period end must be after period start.");

        User owner = currentUser();

        budgetRepo.findByOwnerIdAndCategoryAndPeriodStartAndPeriodEnd(
                owner.getId(), req.getCategory(), req.getPeriodStart(), req.getPeriodEnd())
                .ifPresent(b -> { throw new DuplicateResourceException(
                        "A budget for " + req.getCategory() + " in this period already exists."); });

        Budget saved = budgetRepo.save(Budget.builder()
                .owner(owner).category(req.getCategory())
                .limitAmount(req.getLimitAmount())
                .periodStart(req.getPeriodStart()).periodEnd(req.getPeriodEnd())
                .note(req.getNote()).active(true).build());

        return enrichWithSpend(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BudgetResponse> getMyBudgets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("periodStart").descending());
        return PagedResponse.from(
                budgetRepo.findAllByOwnerIdAndActiveTrue(SecurityUtils.currentUserId(), pageable)
                        .map(this::enrichWithSpend));
    }

    @Transactional(readOnly = true)
    public BudgetResponse getById(Long id) {
        return enrichWithSpend(findOwnedOrThrow(id));
    }

    @Transactional
    public BudgetResponse update(Long id, BudgetRequest req) {
        Budget budget = findOwnedOrThrow(id);
        if (req.getPeriodEnd().isBefore(req.getPeriodStart()))
            throw new BadRequestException("Period end must be after period start.");
        budget.setLimitAmount(req.getLimitAmount());
        budget.setPeriodStart(req.getPeriodStart());
        budget.setPeriodEnd(req.getPeriodEnd());
        budget.setNote(req.getNote());
        return enrichWithSpend(budgetRepo.save(budget));
    }

    @Transactional
    public void delete(Long id) {
        Budget budget = findOwnedOrThrow(id);
        budget.setActive(false);
        budgetRepo.save(budget);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BudgetResponse enrichWithSpend(Budget b) {
        BigDecimal spent = recordRepo.sumExpenseByCategoryAndPeriod(
                b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
        BigDecimal remaining = b.getLimitAmount().subtract(spent);
        double pct = spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        return BudgetResponse.builder()
                .id(b.getId()).category(b.getCategory())
                .limitAmount(b.getLimitAmount()).spentAmount(spent)
                .remainingAmount(remaining).usagePercent(pct)
                .status(resolveStatus(pct))
                .periodStart(b.getPeriodStart()).periodEnd(b.getPeriodEnd())
                .note(b.getNote()).active(b.isActive())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private String resolveStatus(double pct) {
        if (pct >= criticalThreshold) return "EXCEEDED";
        if (pct >= warningThreshold)  return "WARNING";
        return "ON_TRACK";
    }

    private Budget findOwnedOrThrow(Long id) {
        return budgetRepo.findByIdAndOwnerId(id, SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    private User currentUser() {
        return userRepo.findByUsername(SecurityUtils.currentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
