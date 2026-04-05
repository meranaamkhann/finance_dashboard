package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.Category;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "1.00", message = "Budget limit must be at least 1")
    @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
    private BigDecimal limitAmount;

    @NotNull(message = "Period start is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;

    @Size(max = 300)
    private String note;
}
