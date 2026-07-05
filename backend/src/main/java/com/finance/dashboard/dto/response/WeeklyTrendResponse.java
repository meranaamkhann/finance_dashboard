package com.finance.dashboard.dto.response;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder
public class WeeklyTrendResponse {
    private String weekLabel;   // e.g. "Week 1 - Jun 2024"
    private int weekOfYear;
    private int year;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}
