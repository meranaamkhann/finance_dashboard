package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class RecurringTransactionRequest {
    @NotBlank @Size(max=200) private String name;
    @NotNull private TransactionType type;
    @NotNull private Category category;
    @NotNull @DecimalMin("0.01") @DecimalMax("999999999.99") @Digits(integer=9,fraction=2) private BigDecimal amount;
    @NotNull private RecurringFrequency frequency;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    @AssertTrue(message="End date must be after start date")
    public boolean isEndDateValid() { return startDate==null||endDate==null||endDate.isAfter(startDate); }
}
