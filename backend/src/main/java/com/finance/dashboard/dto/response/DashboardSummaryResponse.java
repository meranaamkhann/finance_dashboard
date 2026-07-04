package com.finance.dashboard.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
@Data @Builder
public class DashboardSummaryResponse {
    private BigDecimal totalIncome, totalExpense, netBalance, savingsRate;
    private long totalRecords, activeBudgets, activeRecurring, unreadNotifications;
    private FinancialHealthScoreResponse healthScore;
    private List<CategorySummaryResponse> topCategories;
}
