package com.finance.dashboard.repository;

import com.finance.dashboard.model.Payment;
import com.finance.dashboard.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Payment> findByRazorpayOrderId(String orderId);
    Optional<Payment> findByRazorpayPaymentId(String paymentId);
    long countByUserIdAndStatus(Long userId, PaymentStatus status);
}