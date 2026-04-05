package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Builder
public class FinancialRecordResponse {
    private Long            id;
    private BigDecimal      amount;
    private TransactionType type;
    private Category        category;
    private LocalDate       date;
    private String          description;
    private String          tags;
    private String          createdByUsername;
    private Long            recurringRuleId;
    private LocalDateTime   createdAt;
    private LocalDateTime   updatedAt;
}
