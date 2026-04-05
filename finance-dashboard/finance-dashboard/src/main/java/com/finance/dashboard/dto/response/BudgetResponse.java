package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Category;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class BudgetResponse {
    private Long       id;
    private Category   category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double     usagePercent;
    private String     status;          // "ON_TRACK", "WARNING", "CRITICAL", "EXCEEDED"
    private LocalDate  periodStart;
    private LocalDate  periodEnd;
    private String     note;
    private boolean    active;
    private LocalDateTime createdAt;
}
