package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConfig;
import com.finance.dashboard.dto.response.*;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository  recordRepo;
    private final BudgetRepository           budgetRepo;
    private final NotificationRepository     notifRepo;
    private final FinancialRecordService     recordService;
    private final BudgetService              budgetService;
    private final FinancialHealthScoreService healthScoreService;

    @Cacheable(value = CacheConfig.CACHE_DASHBOARD, key = "'summary'")
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal income   = recordRepo.sumByType(TransactionType.INCOME);
        BigDecimal expenses = recordRepo.sumByType(TransactionType.EXPENSE);
        long totalRecords   = recordRepo.findAllByDeletedFalseOrderByDateDescCreatedAtDesc(
                PageRequest.of(0, 1)).getTotalElements();
        long unread         = notifRepo.countByUserIdAndIsReadFalse(SecurityUtils.currentUserId());

        return DashboardSummaryResponse.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .netBalance(income.subtract(expenses))
                .totalRecords(totalRecords)
                .unreadNotifications(unread)
                .categoryWiseTotals(buildCategoryMap())
                .recentTransactions(
                        recordRepo.findAllByDeletedFalseOrderByDateDescCreatedAtDesc(PageRequest.of(0, 10))
                                .stream().map(recordService::toResponse).toList())
                .monthlyTrends(buildMonthlyTrends(6))
                .activeBudgets(budgetService.getMyBudgets(0, 10).getContent())
                .healthScore(healthScoreService.compute())
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getSummaryByRange(LocalDate from, LocalDate to) {
        BigDecimal income   = recordRepo.sumByTypeAndDateRange(TransactionType.INCOME,  from, to);
        BigDecimal expenses = recordRepo.sumByTypeAndDateRange(TransactionType.EXPENSE, from, to);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        result.put("totalIncome",   income);
        result.put("totalExpenses", expenses);
        result.put("netBalance",    income.subtract(expenses));
        result.put("savingsRate",   income.compareTo(BigDecimal.ZERO) > 0
                ? income.subtract(expenses).divide(income, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return result;
    }

    @Cacheable(value = CacheConfig.CACHE_CATEGORY, key = "'all'")
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategorySummary() {
        List<Object[]> rows = recordRepo.getCategoryWiseTotals();
        BigDecimal totalIncome   = recordRepo.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepo.sumByType(TransactionType.EXPENSE);

        return rows.stream().map(row -> {
            String type   = row[1].toString();
            BigDecimal amt = new BigDecimal(row[2].toString());
            BigDecimal total = "INCOME".equals(type) ? totalIncome : totalExpenses;
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amt.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return CategorySummaryResponse.builder()
                    .category(row[0].toString())
                    .type(TransactionType.valueOf(type))
                    .total(amt)
                    .percentageOfType(Math.round(pct * 100.0) / 100.0)
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrends(int months) {
        return buildMonthlyTrends(months);
    }

    @Transactional(readOnly = true)
    public List<WeeklyTrendResponse> getWeeklyTrends(int weeks) {
        LocalDate from = LocalDate.now().minusWeeks(weeks);
        List<Object[]> rows = recordRepo.getWeeklyTotals(from);
        Map<String, WeeklyTrendResponse> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String key = row[1] + "-W" + String.format("%02d", ((Number) row[0]).intValue());
            map.computeIfAbsent(key, k -> WeeklyTrendResponse.builder()
                    .weekLabel(k).income(BigDecimal.ZERO).expenses(BigDecimal.ZERO).build());
            WeeklyTrendResponse entry = map.get(key);
            BigDecimal amount = new BigDecimal(row[3].toString());
            if ("INCOME".equals(row[2].toString())) entry.setIncome(amount);
            else                                    entry.setExpenses(amount);
        }
        map.values().forEach(e -> e.setNet(e.getIncome().subtract(e.getExpenses())));
        return new ArrayList<>(map.values());
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTopExpenseCategories(int limit) {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.withDayOfMonth(1);
        return recordRepo.getTopExpenseCategories(from, to, limit);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSpendingByDayOfWeek() {
        return recordRepo.getSpendingByDayOfWeek();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, BigDecimal> buildCategoryMap() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        recordRepo.getCategoryWiseTotals()
                .forEach(row -> totals.put(row[0] + "_" + row[1], new BigDecimal(row[2].toString())));
        return totals;
    }

    private List<MonthlyTrendResponse> buildMonthlyTrends(int months) {
        LocalDate from = LocalDate.now().minusMonths(months).withDayOfMonth(1);
        List<Object[]> rows = recordRepo.getMonthlyTotals(from);
        Map<String, MonthlyTrendResponse> map = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String month = row[0].toString();
            map.computeIfAbsent(month, k -> MonthlyTrendResponse.builder()
                    .month(k).income(BigDecimal.ZERO).expenses(BigDecimal.ZERO).build());
            MonthlyTrendResponse entry = map.get(month);
            BigDecimal amount = new BigDecimal(row[2].toString());
            if ("INCOME".equals(row[1].toString())) entry.setIncome(amount);
            else                                    entry.setExpenses(amount);
        }
        map.values().forEach(e -> {
            e.setNet(e.getIncome().subtract(e.getExpenses()));
            e.setSavingsRate(e.getIncome().compareTo(BigDecimal.ZERO) > 0
                    ? e.getNet().divide(e.getIncome(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
        });
        return new ArrayList<>(map.values());
    }
}
