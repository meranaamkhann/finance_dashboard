package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.enums.BillingCycle;
import com.finance.dashboard.model.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class SubscriptionResponse {
    private Long id;
    private PlanResponse plan;
    private SubscriptionStatus status;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate trialEndDate;
    private boolean autoRenew;
    private boolean active;
    private boolean inTrial;
    private int daysRemaining;
    private LocalDateTime createdAt;
}