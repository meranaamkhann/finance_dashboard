package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter @Builder
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long       totalRecords;
    private long       unreadNotifications;
    private Map<String, BigDecimal>        categoryWiseTotals;
    private List<FinancialRecordResponse>  recentTransactions;
    private List<MonthlyTrendResponse>     monthlyTrends;
    private List<BudgetResponse>           activeBudgets;
    private FinancialHealthScoreResponse   healthScore;
}
