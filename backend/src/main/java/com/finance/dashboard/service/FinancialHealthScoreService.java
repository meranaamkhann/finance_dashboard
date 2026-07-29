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

    @Transactional(readOnly = true)
    public FinancialHealthScoreResponse calculate(Long userId) {
        LocalDate from = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        Map<String, BigDecimal> incomeMap  = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.INCOME,  from));
        Map<String, BigDecimal> expenseMap = toMonthMap(recordRepository.monthlyAmountByTypeAndUser(userId, TransactionType.EXPENSE, from));
        Set<String> allMonths = new LinkedHashSet<>(incomeMap.keySet()); allMonths.addAll(expenseMap.keySet());
        if (allMonths.isEmpty())
            return FinancialHealthScoreResponse.builder().score(0).grade("N/A").breakdown(Map.of())
                    .insights(List.of("No data in last 6 months. Add income and expenses to get your score.")).build();

        List<Double> incomes = new ArrayList<>(), expenses = new ArrayList<>(), nets = new ArrayList<>();
        double totalIncome = 0, totalExpense = 0;
        for (String m : allMonths) {
            double inc = incomeMap.getOrDefault(m, BigDecimal.ZERO).doubleValue();
            double exp = expenseMap.getOrDefault(m, BigDecimal.ZERO).doubleValue();
            incomes.add(inc); expenses.add(exp); nets.add(inc - exp);
            totalIncome += inc; totalExpense += exp;
        }
        double savingsRate = totalIncome > 0 ? (totalIncome - totalExpense) / totalIncome * 100 : 0;
        double savingsPts  = clamp(savingsRate / 30.0 * 30, 0, 30);
        double budgetPts   = calcBudgetAdherence(userId) * 25;
        double hhiPts      = calcHHI(userId) * 20;
        double incCv       = cv(incomes);
        double incPts      = clamp((1.0 - Math.min(incCv, 1.0)) * 15, 0, 15);
        long   posMonths   = nets.stream().filter(n -> n >= 0).count();
        double netPts      = (double) posMonths / nets.size() * 10;
        int score = Math.max(0, Math.min(100, (int) Math.round(savingsPts + budgetPts + hhiPts + incPts + netPts)));

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Savings Rate (max 30)",       r2(savingsPts));
        breakdown.put("Budget Adherence (max 25)",   r2(budgetPts));
        breakdown.put("Expense Diversity (max 20)",  r2(hhiPts));
        breakdown.put("Income Stability (max 15)",   r2(incPts));
        breakdown.put("Positive Cash Flow (max 10)", r2(netPts));
        return FinancialHealthScoreResponse.builder().score(score).grade(grade(score))
                .breakdown(breakdown).insights(insights(savingsRate, budgetPts/25, hhiPts/20, posMonths, nets.size())).build();
    }

    private double calcBudgetAdherence(Long userId) {
        var budgets = budgetRepository.findAllByUserIdAndActiveTrue(userId);
        if (budgets.isEmpty()) return 0.5;
        long ok = budgets.stream().filter(b -> {
            BigDecimal spent = recordRepository.spentByUserCategoryAndPeriod(
                    userId, b.getCategory(), b.getPeriodStart(), b.getPeriodEnd());
            if (spent == null) return true;
            return spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue() < 80;
        }).count();
        return (double) ok / budgets.size();
    }

    private double calcHHI(Long userId) {
        var cats = recordRepository.categoryBreakdownByUser(
                userId, LocalDate.now().withDayOfMonth(1), LocalDate.now());
        if (cats.isEmpty()) return 0.5;
        double total = cats.stream().mapToDouble(r -> new BigDecimal(r[1].toString()).doubleValue()).sum();
        if (total == 0) return 0.5;
        double hhi = cats.stream().mapToDouble(r -> {
            double s = new BigDecimal(r[1].toString()).doubleValue() / total; return s * s;
        }).sum();
        return clamp(1.0 - hhi, 0, 1);
    }

    private Map<String, BigDecimal> toMonthMap(List<Object[]> rows) {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        for (Object[] r : rows) m.put(r[0] + "-" + r[1], new BigDecimal(r[2].toString()));
        return m;
    }
    private double cv(List<Double> vals) {
        if (vals.size() < 2) return 0;
        double mean = vals.stream().mapToDouble(d -> d).average().orElse(0);
        if (mean == 0) return 0;
        return Math.sqrt(vals.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0)) / mean;
    }
    private double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private double r2(double v) { return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue(); }
    private String grade(int s)  { return s >= 85 ? "A" : s >= 70 ? "B" : s >= 55 ? "C" : s >= 40 ? "D" : "F"; }

    private List<String> insights(double sr, double budgetAdh, double diversity, long pos, int total) {
        List<String> i = new ArrayList<>();
        if (sr < 0)         i.add("Spending exceeds income. Immediate action needed.");
        else if (sr < 10)   i.add("Savings rate below 10%. Target at least 30% of income.");
        else if (sr < 20)   i.add("Good start — push savings above 20% for security.");
        else                i.add(String.format("Excellent savings rate of %.1f%%. Keep it up!", sr));
        if (budgetAdh < 0.5) i.add("Most budgets are near their limit. Review spending.");
        if (diversity < 0.5) i.add("Expenses concentrated in few categories. Diversify spending.");
        if (pos < total)    i.add(String.format("%d month(s) with negative net. Build an emergency fund.", total - pos));
        if (i.stream().noneMatch(s -> s.contains("immediate") || s.contains("negative") || s.contains("concentrated")))
            i.add("Outstanding financial health! Maintain these excellent habits.");
        return i;
    }
}
