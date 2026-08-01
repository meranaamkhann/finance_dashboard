package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalSubscriptions;
    private long activeSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private Map<String, Long> usersByRole;
    private Map<String, Long> subscriptionsByPlan;
    private Map<String, BigDecimal> revenueByMonth;
    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
}