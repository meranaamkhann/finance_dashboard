package com.finance.dashboard.service;

import com.finance.dashboard.config.CacheConfig;
import com.finance.dashboard.dto.response.FinancialHealthScoreResponse;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Computes a composite financial health score (0–100) from five weighted signals.
 * Results are cached and evicted whenever records change (see FinancialRecordService).
 *
 * Signal breakdown:
 *   Savings rate     30 pts — net / income  (full points at ≥ 30%)
 *   Budget adherence 25 pts — budgets on track vs total active
 *   Expense diversity20 pts — Herfindahl index across categories (lower = better)
 *   Income stability 15 pts — stddev of monthly income (lower = better)
 *   Positive months  10 pts — months with net > 0 over last 6
 */
@Service
@RequiredArgsConstructor
public class FinancialHealthScoreService {

    private final FinancialRecordRepository recordRepo;
    private final BudgetRepository          budgetRepo;

    @Cacheable(value = CacheConfig.CACHE_HEALTH_SCORE, key = "'global'")
    @Transactional(readOnly = true)
    public FinancialHealthScoreResponse compute() {
        LocalDate today    = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6).withDayOfMonth(1);

        BigDecimal totalIncome   = recordRepo.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepo.sumByType(TransactionType.EXPENSE);

        // ── Signal 1: Savings rate (30 pts) ──────────────────────────────────
        int savingsScore = 0;
        BigDecimal savingsRate = BigDecimal.ZERO;
        BigDecimal net = totalIncome.subtract(totalExpenses);
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = net.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            double sr = savingsRate.doubleValue();
            savingsScore = sr >= 30 ? 30 : sr <= 0 ? 0 : (int)(sr / 30.0 * 30);
        }

        // ── Signal 2: Budget adherence (25 pts) ──────────────────────────────
        int budgetScore = 25; // full marks if no budgets defined
        List<Object[]> categoryTotals = recordRepo.getCategoryWiseTotals();
        var activeBudgets = budgetRepo.findAllActiveBudgetsForDate(today);
        if (!activeBudgets.isEmpty()) {
            long onTrack = activeBudgets.stream().filter(b -> {
                BigDecimal spent = recordRepo.sumExpenseByCategoryAndPeriod(
                        b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
                return spent.compareTo(b.getLimitAmount()) <= 0;
            }).count();
            budgetScore = (int)((double) onTrack / activeBudgets.size() * 25);
        }

        // ── Signal 3: Expense diversity (20 pts) ─────────────────────────────
        int diversityScore = computeDiversityScore(categoryTotals, totalExpenses);

        // ── Signal 4: Income stability (15 pts) ──────────────────────────────
        int stabilityScore = computeStabilityScore(sixMonthsAgo);

        // ── Signal 5: Positive cash-flow months (10 pts) ─────────────────────
        int cashFlowScore = computeCashFlowScore(sixMonthsAgo);

        int overall = savingsScore + budgetScore + diversityScore + stabilityScore + cashFlowScore;
        overall = Math.max(0, Math.min(100, overall));

        // Monthly savings for context
        BigDecimal avgMonthlySavings = net.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        return FinancialHealthScoreResponse.builder()
                .overallScore(overall)
                .grade(grade(overall))
                .summary(summary(overall))
                .savingsScore(savingsScore)
                .budgetScore(budgetScore)
                .diversityScore(diversityScore)
                .stabilityScore(stabilityScore)
                .cashFlowScore(cashFlowScore)
                .savingsRate(savingsRate.setScale(2, RoundingMode.HALF_UP))
                .avgMonthlySavings(avgMonthlySavings)
                .insights(buildInsights(savingsScore, budgetScore, diversityScore,
                        stabilityScore, cashFlowScore, savingsRate.doubleValue()))
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int computeDiversityScore(List<Object[]> categoryTotals, BigDecimal totalExpenses) {
        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) return 20;
        // Herfindahl-Hirschman Index: sum of (share)^2, lower = more diverse
        double hhi = 0.0;
        for (Object[] row : categoryTotals) {
            if ("EXPENSE".equals(row[1].toString())) {
                double share = new BigDecimal(row[2].toString())
                        .divide(totalExpenses, 6, RoundingMode.HALF_UP).doubleValue();
                hhi += share * share;
            }
        }
        // HHI ranges 0 (perfect diversity) to 1 (monopoly). Score inversely.
        return (int)((1.0 - Math.min(hhi, 1.0)) * 20);
    }

    private int computeStabilityScore(LocalDate from) {
        List<Object[]> monthly = recordRepo.getMonthlyTotalsByType("INCOME", 6);
        if (monthly.size() < 2) return 10; // not enough data — partial credit
        double[] amounts = monthly.stream()
                .mapToDouble(r -> Double.parseDouble(r[1].toString())).toArray();
        double mean   = Arrays.stream(amounts).average().orElse(0);
        double variance = Arrays.stream(amounts).map(a -> (a - mean) * (a - mean))
                .average().orElse(0);
        double stddev  = Math.sqrt(variance);
        double cv      = mean > 0 ? stddev / mean : 1.0; // coefficient of variation
        // CV ≤ 0.05 → 15 pts, CV ≥ 0.5 → 0 pts
        int score = (int)((1.0 - Math.min(cv / 0.5, 1.0)) * 15);
        return Math.max(0, score);
    }

    private int computeCashFlowScore(LocalDate from) {
        List<Object[]> monthly = recordRepo.getMonthlyTotals(from);
        Map<String, double[]> byMonth = new LinkedHashMap<>();
        for (Object[] row : monthly) {
            String month  = row[0].toString();
            String type   = row[1].toString();
            double amount = Double.parseDouble(row[2].toString());
            byMonth.computeIfAbsent(month, k -> new double[]{0, 0});
            if ("INCOME".equals(type))  byMonth.get(month)[0] += amount;
            else                        byMonth.get(month)[1] += amount;
        }
        long positiveMonths = byMonth.values().stream()
                .filter(arr -> arr[0] - arr[1] > 0).count();
        int total = Math.max(byMonth.size(), 1);
        return (int)((double) positiveMonths / total * 10);
    }

    private String grade(int score) {
        if (score >= 85) return "A";
        if (score >= 70) return "B";
        if (score >= 55) return "C";
        if (score >= 40) return "D";
        return "F";
    }

    private String summary(int score) {
        if (score >= 85) return "Excellent financial health! You're saving well and spending wisely.";
        if (score >= 70) return "Good financial health with room for improvement.";
        if (score >= 55) return "Average health — review your budgets and savings rate.";
        if (score >= 40) return "Below average — take action to improve your savings and reduce overspending.";
        return "Poor financial health — immediate attention needed on budgeting and savings.";
    }

    private List<String> buildInsights(int sScore, int bScore, int dScore,
                                        int stScore, int cfScore, double savingsRate) {
        List<String> insights = new ArrayList<>();
        if (sScore < 20)  insights.add("💡 Try to save at least 20–30% of your income each month.");
        if (bScore < 15)  insights.add("📊 Several budgets are being exceeded — review your spending limits.");
        if (dScore < 12)  insights.add("🔀 Your spending is concentrated in few categories — diversify to reduce risk.");
        if (stScore < 10) insights.add("📈 Income variability is high — consider building a 3-month emergency fund.");
        if (cfScore < 7)  insights.add("⚖️  You have negative cash flow in several months — cut discretionary expenses.");
        if (savingsRate < 10) insights.add("🏦 Your savings rate is critically low. Automate savings to improve consistency.");
        if (insights.isEmpty()) insights.add("🎉 Great job! Keep maintaining your healthy financial habits.");
        return insights;
    }
}
