package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @Builder
public class RecurringTransactionResponse {
    private Long id;
    private String name;
    private TransactionType type;
    private Category category;
    private BigDecimal amount;
    private RecurringFrequency frequency;
    private LocalDate startDate, endDate, nextExecutionDate, lastExecutedDate;
    private boolean active;
    private LocalDateTime createdAt;
}
