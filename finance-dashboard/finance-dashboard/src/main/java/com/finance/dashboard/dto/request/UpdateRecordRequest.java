package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRecordRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount format invalid")
    private BigDecimal amount;

    private TransactionType type;
    private Category category;

    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;

    @Size(max = 500)
    private String description;

    @Size(max = 255)
    private String tags;
}
