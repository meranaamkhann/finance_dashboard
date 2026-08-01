package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class PlanResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private int maxRecords;
    private int maxBudgets;
    private int maxRecurring;
    private int maxExports;
    private int maxUsers;
    private boolean apiAccess;
    private boolean prioritySupport;
    private int sortOrder;
    private List<String> features;
}