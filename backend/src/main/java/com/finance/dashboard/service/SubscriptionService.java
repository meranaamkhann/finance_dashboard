package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.SubscriptionResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Plan;
import com.finance.dashboard.model.Subscription;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.BillingCycle;
import com.finance.dashboard.model.enums.SubscriptionStatus;
import com.finance.dashboard.repository.PlanRepository;
import com.finance.dashboard.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PlanService planService;

    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Plan getActivePlan(Long userId) {
        return getActiveSubscription(userId)
                .map(Subscription::getPlan)
                .orElse(planRepository.findBySlug("free")
                        .orElseThrow(() -> new ResourceNotFoundException("Free plan not found")));
    }

    @Transactional
    public Subscription activate(User user, Plan plan, BillingCycle cycle) {
        getActiveSubscription(user.getId()).ifPresent(s -> {
            s.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(s);
        });

        LocalDate start = LocalDate.now();
        LocalDate end   = cycle == BillingCycle.YEARLY ? start.plusYears(1) : start.plusMonths(1);

        Subscription sub = Subscription.builder()
                .user(user).plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(cycle)
                .startDate(start).endDate(end)
                .build();
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void cancel(Long userId, String reason) {
        Subscription sub = getActiveSubscription(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription"));
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setCancelledAt(LocalDate.now());
        sub.setCancellationReason(reason);
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getHistory(Long userId) {
        return subscriptionRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        return getActiveSubscription(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    public void checkRecordLimit(Long userId) {
        Plan plan = getActivePlan(userId);
        if (plan.getMaxRecords() < 0) return;
        long count = subscriptionRepository.findActiveByUserId(userId, LocalDate.now())
                .map(s -> s.getId()).orElse(0L);
        if (plan.getMaxRecords() != Integer.MAX_VALUE) {
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpired(LocalDate.now());
        expired.forEach(s -> {
            s.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(s);
            log.info("Subscription {} expired for user {}", s.getId(), s.getUser().getId());
        });
    }

    public SubscriptionResponse toResponse(Subscription s) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), s.getEndDate());
        return SubscriptionResponse.builder()
                .id(s.getId()).plan(planService.toResponse(s.getPlan()))
                .status(s.getStatus()).billingCycle(s.getBillingCycle())
                .startDate(s.getStartDate()).endDate(s.getEndDate())
                .trialEndDate(s.getTrialEndDate()).autoRenew(s.isAutoRenew())
                .active(s.isActive()).inTrial(s.isInTrial())
                .daysRemaining((int) Math.max(0, days))
                .createdAt(s.getCreatedAt())
                .build();
    }
}