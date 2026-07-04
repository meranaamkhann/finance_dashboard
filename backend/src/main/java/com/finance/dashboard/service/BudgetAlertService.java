package com.finance.dashboard.service;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class BudgetAlertService {
    private final BudgetRepository budgetRepository;
    private final FinancialRecordRepository recordRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final AuditService auditService;

    @Value("${app.budget.warning-threshold:80}") private double warningThreshold;
    @Value("${app.budget.critical-threshold:100}") private double criticalThreshold;

    @Transactional
    public void evaluate(User user, Category category, LocalDate date) {
        budgetRepository.findActiveBudgetsForUserCategoryAndDate(user.getId(), category, date)
                .forEach(b -> evaluateBudget(user, b));
    }

    @Transactional
    public void sweepAll() {
        List<Budget> all = budgetRepository.findAllByActiveTrue();
        all.forEach(b -> evaluateBudget(b.getUser(), b));
        log.info("Budget sweep done: {} budgets evaluated", all.size());
    }

    private void evaluateBudget(User user, Budget budget) {
        BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(
                user.getId(), budget.getCategory(), budget.getPeriodStart(), budget.getPeriodEnd());
        if (spent == null) spent = BigDecimal.ZERO;

        double pct = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        String cat = budget.getCategory().name();
        String period = budget.getPeriodStart() + " – " + budget.getPeriodEnd();

        if (pct >= criticalThreshold) {
            if (!recentAlert(user.getId(), NotificationType.BUDGET_EXCEEDED, budget.getId())) {
                notificationService.send(user, NotificationType.BUDGET_EXCEEDED,
                        String.format("🚨 BUDGET EXCEEDED: ₹%.2f of ₹%.2f spent for %s (%s). %.1f%% used.",
                                spent, budget.getLimitAmount(), cat, period, pct));
                auditService.log(AuditAction.BUDGET_EXCEEDED, user.getUsername(), "Budget", budget.getId(),
                        null, null, null, String.format("%.1f%% for %s", pct, cat));
            }
        } else if (pct >= warningThreshold) {
            NotificationType type = pct >= 90 ? NotificationType.BUDGET_CRITICAL : NotificationType.BUDGET_WARNING;
            AuditAction action = pct >= 90 ? AuditAction.BUDGET_CRITICAL : AuditAction.BUDGET_WARNING;
            if (!recentAlert(user.getId(), type, budget.getId())) {
                notificationService.send(user, type,
                        String.format("⚠️ BUDGET %s: %.1f%% used (₹%.2f of ₹%.2f) for %s (%s)",
                                pct >= 90 ? "CRITICAL" : "WARNING", pct, spent, budget.getLimitAmount(), cat, period));
                auditService.log(action, user.getUsername(), "Budget", budget.getId(),
                        null, null, null, String.format("%.1f%% for %s", pct, cat));
            }
        }
    }

    private boolean recentAlert(Long userId, NotificationType type, Long budgetId) {
        return notificationRepository.existsByUserIdAndTypeAndReadFalseAndCreatedAtAfterAndMessageContaining(
                userId, type, LocalDateTime.now().minusHours(24), "Budget ID:" + budgetId);
    }
}
