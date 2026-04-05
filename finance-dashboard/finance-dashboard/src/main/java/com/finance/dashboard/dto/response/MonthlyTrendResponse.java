package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class MonthlyTrendResponse {
    private String     month;       // "yyyy-MM"
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;
    private BigDecimal savingsRate; // net/income * 100
}
