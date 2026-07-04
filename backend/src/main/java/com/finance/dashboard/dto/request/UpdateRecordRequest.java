package com.finance.dashboard.dto.request;
import com.finance.dashboard.model.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class UpdateRecordRequest {
    private TransactionType type;
    private Category category;
    @DecimalMin("0.01") @DecimalMax("999999999.99") @Digits(integer=9,fraction=2) private BigDecimal amount;
    @PastOrPresent private LocalDate date;
    @Size(max=500) private String description;
    @Size(max=300) private String tags;
}
