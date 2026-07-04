package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Category;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder
public class BudgetResponse {
    private Long id;
    private Category category;
    private BigDecimal limitAmount, spentAmount, remainingAmount;
    private double usagePercent;
    private String status;
    private LocalDate periodStart, periodEnd;
    private boolean active;
    private LocalDateTime createdAt;
}
