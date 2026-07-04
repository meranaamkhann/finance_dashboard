package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.Category;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class BudgetRequest {
    @NotNull private Category category;
    @NotNull @DecimalMin("1.00") @DecimalMax("999999999.99") @Digits(integer=9,fraction=2) private BigDecimal limitAmount;
    @NotNull private LocalDate periodStart;
    @NotNull private LocalDate periodEnd;
    @AssertTrue(message="Period end must be after period start")
    public boolean isPeriodValid() { return periodStart==null||periodEnd==null||periodEnd.isAfter(periodStart); }
}
