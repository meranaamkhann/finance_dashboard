package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * A composite financial health score (0–100) derived from five weighted signals:
 *
 *  1. Savings rate           (30 pts) — what % of income is saved
 *  2. Budget adherence       (25 pts) — are active budgets being respected
 *  3. Expense diversity      (20 pts) — spending spread across categories (lower concentration = better)
 *  4. Income stability       (15 pts) — month-over-month income variance
 *  5. Positive cash flow     (10 pts) — months with net > 0 over last 6 months
 */
@Getter @Builder
public class FinancialHealthScoreResponse {
    private int            overallScore;     // 0–100
    private String         grade;            // A, B, C, D, F
    private String         summary;          // human-readable verdict
    private int            savingsScore;
    private int            budgetScore;
    private int            diversityScore;
    private int            stabilityScore;
    private int            cashFlowScore;
    private BigDecimal     savingsRate;      // percentage
    private BigDecimal     avgMonthlySavings;
    private List<String>   insights;         // actionable recommendations
}
