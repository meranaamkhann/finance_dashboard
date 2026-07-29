package com.finance.dashboard.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class WeeklyTrendResponse {
    private int        year;
    private int        weekOfYear;
    private String     weekLabel;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}
