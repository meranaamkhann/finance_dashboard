package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Category;
import com.finance.dashboard.model.enums.RecurringFrequency;
import com.finance.dashboard.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class RecurringTransactionResponse {
    private Long              id;
    private String            name;
    private TransactionType   type;
    private Category          category;
    private BigDecimal        amount;
    private RecurringFrequency frequency;
    private LocalDate         startDate;
    private LocalDate         endDate;
    private LocalDate         nextExecutionDate;
    private LocalDate         lastExecutedDate;
    private boolean           active;
    private LocalDateTime     createdAt;
}
