package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.RecurringFrequency;
import com.finance.dashboard.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class RecurringTransactionResponse {
    private Long               id;
    private String             name;
    private BigDecimal         amount;
    private TransactionType    type;
    private Category           category;
    private RecurringFrequency frequency;
    private LocalDate          startDate;
    private LocalDate          endDate;
    private LocalDate          lastExecutedDate;
    private LocalDate          nextExecutionDate;
    private String             description;
    private boolean            active;
    private LocalDateTime      createdAt;
}
