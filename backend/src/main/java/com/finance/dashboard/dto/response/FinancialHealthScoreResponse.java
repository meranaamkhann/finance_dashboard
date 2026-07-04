package com.finance.dashboard.dto.response;
import lombok.*;
import java.util.List;
import java.util.Map;
@Data @Builder
public class FinancialHealthScoreResponse {
    private int score;
    private String grade;
    private Map<String,Double> breakdown;
    private List<String> insights;
}
