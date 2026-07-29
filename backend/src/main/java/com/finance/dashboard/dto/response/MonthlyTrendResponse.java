package com.finance.dashboard.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class MonthlyTrendResponse {
    private int        year;
    private int        month;
    private String     monthLabel;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;
}
