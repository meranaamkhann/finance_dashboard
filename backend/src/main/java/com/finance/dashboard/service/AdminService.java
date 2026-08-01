package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.AdminDashboardResponse;
import com.finance.dashboard.model.enums.PaymentStatus;
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.repository.PaymentRepository;
import com.finance.dashboard.repository.PlanRepository;
import com.finance.dashboard.repository.SubscriptionRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository       userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository    paymentRepository;
    private final PlanRepository       planRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getDashboard() {
        long totalUsers  = userRepository.count();
        long activeUsers = userRepository.findAllByDeletedFalse(org.springframework.data.domain.Pageable.unpaged()).getTotalElements();

        long totalSubs  = subscriptionRepository.count();
        long activeSubs = subscriptionRepository.findExpired(LocalDate.now().plusYears(10)).size();

        BigDecimal totalRevenue   = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        BigDecimal monthRev  = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS
                        && p.getPaidAt() != null
                        && p.getPaidAt().toLocalDate().isAfter(monthStart.minusDays(1)))
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalPay   = paymentRepository.count();
        long successPay = paymentRepository.countByUserIdAndStatus(0L, PaymentStatus.SUCCESS);
        long failedPay  = paymentRepository.countByUserIdAndStatus(0L, PaymentStatus.FAILED);

        Map<String, Long> byRole = new LinkedHashMap<>();
        for (Role r : Role.values()) {
            byRole.put(r.name(), userRepository.findAllByRoleAndDeletedFalse(
                    r, org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
        }

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalSubscriptions(totalSubs)
                .activeSubscriptions(subscriptionRepository
                        .findActiveByUserId(-1L, LocalDate.now()).isEmpty() ? 0L : 0L)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthRev)
                .yearlyRevenue(BigDecimal.ZERO)
                .usersByRole(byRole)
                .totalPayments(totalPay)
                .successfulPayments(successPay)
                .failedPayments(failedPay)
                .build();
    }
}