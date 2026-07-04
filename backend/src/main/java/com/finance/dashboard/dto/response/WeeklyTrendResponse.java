package com.finance.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class WeeklyTrendResponse {
    private String     weekLabel;  // "2024-W14"
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal net;
}
