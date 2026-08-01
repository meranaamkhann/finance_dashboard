package com.finance.dashboard.dto.request;

import com.finance.dashboard.model.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "Plan slug is required")
    private String planSlug;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;
}