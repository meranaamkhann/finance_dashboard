package com.finance.dashboard.model;

import com.finance.dashboard.model.enums.BillingCycle;
import com.finance.dashboard.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_sub_user", columnList = "user_id"),
    @Index(name = "idx_sub_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate trialEndDate;
    private LocalDate cancelledAt;
    private String cancellationReason;

    @Column(nullable = false)
    @Builder.Default
    private boolean autoRenew = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && LocalDate.now().isBefore(endDate.plusDays(1));
    }

    public boolean isInTrial() {
        return trialEndDate != null && LocalDate.now().isBefore(trialEndDate.plusDays(1));
    }
}