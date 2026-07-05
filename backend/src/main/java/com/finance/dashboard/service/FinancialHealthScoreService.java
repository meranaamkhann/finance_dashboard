package com.finance.dashboard.service;
import com.finance.dashboard.dto.response.FinancialHealthScoreResponse;
import com.finance.dashboard.model.enums.TransactionType;
import com.finance.dashboard.repository.BudgetRepository;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class FinancialHealthScoreService {
    private final FinancialRecordRepository recordRepository;
    private final BudgetRepository budgetRepository;

    /**
     * 5-signal composite score (0–100) matching the README:
     *  1. Savings rate       — 30 pts  (net/income; full at ≥ 30%)
     *  2. Budget adherence   — 25 pts  (% active budgets ON_TRACK)
     *  3. Expense diversity  — 20 pts  (Herfindahl-Hirschman Index; lower concentration = better)
     *  4. Income stability   — 15 pts  (low coefficient of variation)
     *  5. Positive cash flow — 10 pts  (months with net > 0 over last 6)
     */
    @Transactional(readOnly=true)
    public FinancialHealthScoreResponse calculate(Long userId) {
        LocalDate from = LocalDate.now().minusMonths(6).withDayOfMonth(1);

        Map<String, BigDecimal> incomeMap  = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.INCOME,  from));
        Map<String, BigDecimal> expenseMap = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.EXPENSE, from));
        Set<String> allMonths = new LinkedHashSet<>(incomeMap.keySet()); allMonths.addAll(expenseMap.keySet());

        if (allMonths.isEmpty())
            return FinancialHealthScoreResponse.builder().score(0).grade("N/A").breakdown(Map.of())
                    .insights(List.of("No data in the last 6 months. Record income and expenses to get your score.")).build();

        List<Double> incomes=new ArrayList<>(), expenses=new ArrayList<>(), nets=new ArrayList<>();
        double totalIncome=0, totalExpense=0;
        for (String m : allMonths) {
            double inc = incomeMap.getOrDefault(m, BigDecimal.ZERO).doubleValue();
            double exp = expenseMap.getOrDefault(m, BigDecimal.ZERO).doubleValue();
            incomes.add(inc); expenses.add(exp); nets.add(inc - exp);
            totalIncome += inc; totalExpense += exp;
        }

        // ── Signal 1: Savings Rate (max 30 pts) ──────────────────────────
        double savingsRate = totalIncome > 0 ? (totalIncome - totalExpense) / totalIncome * 100 : 0;
        double savingsPts  = clamp(savingsRate / 30.0 * 30, 0, 30);  // full pts at 30%+

        // ── Signal 2: Budget Adherence (max 25 pts) ───────────────────────
        double budgetPts = calculateBudgetAdherence(userId) * 25;

        // ── Signal 3: Expense Diversity — HHI (max 20 pts) ───────────────
        double hhiPts = calculateHHIScore(userId) * 20;

        // ── Signal 4: Income Stability (max 15 pts) ───────────────────────
        double incCv   = cv(incomes);
        double incPts  = clamp((1.0 - Math.min(incCv, 1.0)) * 15, 0, 15);

        // ── Signal 5: Positive Cash Flow months (max 10 pts) ─────────────
        long posMonths = nets.stream().filter(n -> n >= 0).count();
        double netPts  = (double) posMonths / nets.size() * 10;

        int score = Math.max(0, Math.min(100,
                (int) Math.round(savingsPts + budgetPts + hhiPts + incPts + netPts)));

        Map<String,Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Savings Rate (max 30 pts)",     r2(savingsPts));
        breakdown.put("Budget Adherence (max 25 pts)", r2(budgetPts));
        breakdown.put("Expense Diversity (max 20 pts)",r2(hhiPts));
        breakdown.put("Income Stability (max 15 pts)", r2(incPts));
        breakdown.put("Positive Cash Flow (max 10 pts)",r2(netPts));

        return FinancialHealthScoreResponse.builder()
                .score(score).grade(grade(score)).breakdown(breakdown)
                .insights(insights(savingsRate, budgetPts/25, hhiPts/20, incCv, posMonths, nets.size()))
                .build();
    }

    private double calculateBudgetAdherence(Long userId) {
        var budgets = budgetRepository.findAllByUserIdAndActiveTrue(userId);
        if (budgets.isEmpty()) return 0.5; // neutral when no budgets set
        // Budget status is computed in BudgetService; here we estimate from spent vs limit
        long onTrack = budgets.stream().filter(b -> {
            try {
                BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(
                        userId, b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
                if (spent == null) spent = BigDecimal.ZERO;
                double pct = spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
                return pct < 80;
            } catch (Exception e) { return true; }
        }).count();
        return budgets.isEmpty() ? 0.5 : (double) onTrack / budgets.size();
    }

    private double calculateHHIScore(Long userId) {
        // HHI = sum of (category_share^2); lower = more diverse = better
        LocalDate from = LocalDate.now().withDayOfMonth(1);
        var cats = recordRepository.categoryBreakdownByUser(userId, from, LocalDate.now());
        if (cats.isEmpty()) return 0.5;
        double total = cats.stream().mapToDouble(r -> new BigDecimal(r[1].toString()).doubleValue()).sum();
        if (total == 0) return 0.5;
        double hhi = cats.stream().mapToDouble(r -> {
            double share = new BigDecimal(r[1].toString()).doubleValue() / total;
            return share * share;
        }).sum();
        // HHI ranges 0 (perfect diversity) to 1 (monopoly). Score = 1 - HHI
        return clamp(1.0 - hhi, 0, 1);
    }

    private Map<String, BigDecimal> toMonthMap(java.util.List<Object[]> rows) {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        for (Object[] r : rows) m.put(r[0]+"-"+r[1], new BigDecimal(r[2].toString()));
        return m;
    }
    private double cv(List<Double> vals) {
        if (vals.size() < 2) return 0;
        double mean = vals.stream().mapToDouble(d -> d).average().orElse(0);
        if (mean == 0) return 0;
        return Math.sqrt(vals.stream().mapToDouble(v -> Math.pow(v-mean,2)).average().orElse(0)) / mean;
    }
    private double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private double r2(double v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue(); }
    private String grade(int s) { return s>=85?"A":s>=70?"B":s>=55?"C":s>=40?"D":"F"; }

    private List<String> insights(double sr, double budgetAdh, double diversity, double incCv, long pos, int total) {
        List<String> i = new ArrayList<>();
        if (sr < 0)         i.add("🚨 Spending exceeds income. Immediate action needed.");
        else if (sr < 10)   i.add("💡 Savings rate is below 10%. Target at least 30% of income.");
        else if (sr < 20)   i.add("📈 Good start — push savings above 20% for financial security.");
        else                i.add(String.format("✅ Excellent savings rate of %.1f%%. Keep it up!", sr));
        if (budgetAdh < 0.5) i.add("⚠️ Most budgets are at warning levels. Review your spending limits.");
        else if (budgetAdh < 0.8) i.add("📊 Some budgets close to limit. Monitor discretionary categories.");
        if (diversity < 0.5) i.add("⚠️ Expenses are heavily concentrated in one category. Diversify spending.");
        if (incCv > 0.4)    i.add("📊 Income is inconsistent. Build a 3–6 month emergency fund.");
        if (pos < total)    i.add(String.format("📉 %d month(s) with negative net. Build emergency reserves.", total - pos));
        if (i.stream().noneMatch(s -> s.startsWith("🚨") || s.startsWith("⚠️") || s.startsWith("📉")))
            i.add("🏆 Outstanding financial health! Maintain these excellent habits.");
        return i;
    }
}
