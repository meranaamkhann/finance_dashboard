package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Category;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class BudgetResponse {
    private Long       id;
    private Category   category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private double     usagePercent;
    private String     status;
    private LocalDate  periodStart;
    private LocalDate  periodEnd;
    private boolean    active;
    private LocalDateTime createdAt;
}
