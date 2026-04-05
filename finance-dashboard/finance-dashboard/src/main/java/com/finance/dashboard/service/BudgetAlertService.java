package com.finance.dashboard.service;

import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Evaluates all active budgets for a given user + category combination
 * and fires in-app notifications when spending crosses the warning or
 * critical thresholds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    @Value("${app.budget.warning-threshold}")
    private int warningThreshold;   // default 80

    @Value("${app.budget.critical-threshold}")
    private int criticalThreshold;  // default 100

    private final BudgetRepository          budgetRepo;
    private final FinancialRecordRepository recordRepo;
    private final NotificationService       notificationService;
    private final AuditService              auditService;

    @Transactional
    public void evaluate(Long userId, Category category, LocalDate today) {
        List<Budget> budgets = budgetRepo.findAllActiveBudgetsForDate(today)
                .stream()
                .filter(b -> b.getOwner().getId().equals(userId) && b.getCategory() == category)
                .toList();

        for (Budget budget : budgets) {
            BigDecimal spent = recordRepo.sumExpenseByCategoryAndPeriod(
                    category, budget.getPeriodStart(), budget.getPeriodEnd());

            double usagePct = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            String statusLabel = resolveStatus(usagePct);
            log.info("Budget check — user={} category={} spent={} limit={} usage={}% status={}",
                    userId, category, spent, budget.getLimitAmount(), usagePct, statusLabel);

            if (usagePct >= criticalThreshold) {
                fireAlert(userId, category, spent, budget.getLimitAmount(), usagePct, true);
            } else if (usagePct >= warningThreshold) {
                fireAlert(userId, category, spent, budget.getLimitAmount(), usagePct, false);
            }
        }
    }

    /** Called nightly by the scheduler to scan ALL active budgets. */
    @Transactional
    public void evaluateAllActive() {
        LocalDate today = LocalDate.now();
        budgetRepo.findAllActiveBudgetsForDate(today).forEach(budget -> {
            BigDecimal spent = recordRepo.sumExpenseByCategoryAndPeriod(
                    budget.getCategory(), budget.getPeriodStart(), budget.getPeriodEnd());
            double usagePct = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            if (usagePct >= warningThreshold) {
                fireAlert(budget.getOwner().getId(), budget.getCategory(),
                        spent, budget.getLimitAmount(), usagePct, usagePct >= criticalThreshold);
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void fireAlert(Long userId, Category category, BigDecimal spent,
                           BigDecimal limit, double pct, boolean critical) {
        String type  = critical ? "BUDGET_CRITICAL" : "BUDGET_WARNING";
        String emoji = critical ? "🚨" : "⚠️";
        String title = emoji + " Budget " + (critical ? "Exceeded" : "Warning") + ": " + category;
        String msg   = "You have spent ₹%s of your ₹%s budget for %s (%.1f%%). %s"
                .formatted(spent.toPlainString(), limit.toPlainString(), category, pct,
                           critical ? "Budget exceeded!" : "Approaching your limit.");

        notificationService.send(userId, title, msg, type);
        auditService.log("SYSTEM", AuditAction.BUDGET_ALERT, "Budget", null,
                "Budget alert fired for userId=" + userId + " category=" + category, null, null, null);
    }

    private String resolveStatus(double pct) {
        if (pct >= criticalThreshold) return "EXCEEDED";
        if (pct >= warningThreshold)  return "WARNING";
        return "ON_TRACK";
    }
}
