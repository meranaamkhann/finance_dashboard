package com.finance.dashboard.dto.response;
import com.finance.dashboard.model.enums.Category;
import lombok.*;
import java.math.BigDecimal;
@Data @Builder
public class CategorySummaryResponse {
    private Category category;
    private BigDecimal amount;
    private double percentage;
}
