package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class CategorySummaryResponse {
    private String          category;
    private TransactionType type;
    private BigDecimal      total;
    private double          percentageOfType; // share within INCOME or EXPENSE totals
}
