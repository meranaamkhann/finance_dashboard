package com.finance.dashboard.dto.response;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder
public class MonthlyTrendResponse {
    private int year, month;
    private String monthLabel;
    private BigDecimal income, expense, net;
}
