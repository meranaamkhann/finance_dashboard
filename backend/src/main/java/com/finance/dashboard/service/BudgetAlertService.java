package com.finance.dashboard.service;
import com.finance.dashboard.model.Budget;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.AuditAction;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.NotificationType;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.NotificationRepository;
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

    @Value("${app.budget.warning-threshold:80}")  private double warningThreshold;
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
        log.info("Budget sweep: {} budgets evaluated", all.size());
    }

    private void evaluateBudget(User user, Budget b) {
        BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(
                user.getId(), b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
        if (spent == null) spent = BigDecimal.ZERO;
        double pct = spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        String cat    = b.getCategory().name();
        String period = b.getPeriodStart() + " to " + b.getPeriodEnd();

        if (pct >= criticalThreshold) {
            if (!recentAlert(user.getId(), NotificationType.BUDGET_EXCEEDED, b.getId())) {
                notificationService.send(user, NotificationType.BUDGET_EXCEEDED,
                        String.format("BUDGET EXCEEDED: Spent Rs.%.0f of Rs.%.0f for %s (%s). %.1f%% used.",
                                spent, b.getLimitAmount(), cat, period, pct));
                auditService.log(AuditAction.BUDGET_EXCEEDED, user.getUsername(),
                        "Budget", b.getId(), null, null, null,
                        String.format("%.1f%% for %s", pct, cat));
            }
        } else if (pct >= warningThreshold) {
            NotificationType type  = pct >= 90 ? NotificationType.BUDGET_CRITICAL : NotificationType.BUDGET_WARNING;
            AuditAction      audit = pct >= 90 ? AuditAction.BUDGET_CRITICAL      : AuditAction.BUDGET_WARNING;
            if (!recentAlert(user.getId(), type, b.getId())) {
                notificationService.send(user, type,
                        String.format("BUDGET %s: %.1f%% used (Rs.%.0f of Rs.%.0f) for %s (%s)",
                                pct >= 90 ? "CRITICAL" : "WARNING", pct,
                                spent, b.getLimitAmount(), cat, period));
                auditService.log(audit, user.getUsername(), "Budget", b.getId(), null, null, null,
                        String.format("%.1f%% for %s", pct, cat));
            }
        }
    }

    private boolean recentAlert(Long userId, NotificationType type, Long budgetId) {
        return notificationRepository
                .existsByUserIdAndTypeAndReadFalseAndCreatedAtAfterAndMessageContaining(
                        userId, type, LocalDateTime.now().minusHours(24), "Budget ID:" + budgetId);
    }
}
