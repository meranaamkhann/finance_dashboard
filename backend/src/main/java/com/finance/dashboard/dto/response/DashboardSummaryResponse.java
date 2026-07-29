package com.finance.dashboard.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class DashboardSummaryResponse {
    private BigDecimal                    totalIncome;
    private BigDecimal                    totalExpense;
    private BigDecimal                    netBalance;
    private BigDecimal                    savingsRate;
    private long                          totalRecords;
    private long                          activeBudgets;
    private long                          activeRecurring;
    private long                          unreadNotifications;
    private FinancialHealthScoreResponse  healthScore;
    private List<CategorySummaryResponse> topCategories;
}
