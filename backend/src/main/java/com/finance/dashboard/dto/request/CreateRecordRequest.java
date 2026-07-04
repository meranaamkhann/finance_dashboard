package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class CreateRecordRequest {
    @NotNull private TransactionType type;
    @NotNull private Category category;
    @NotNull @DecimalMin("0.01") @DecimalMax("999999999.99") @Digits(integer=9,fraction=2) private BigDecimal amount;
    @NotNull @PastOrPresent(message="Date cannot be in the future") private LocalDate date;
    @Size(max=500) private String description;
    @Size(max=300) private String tags;
}
