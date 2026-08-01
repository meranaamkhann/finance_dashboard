package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.CreateOrderRequest;
import com.finance.dashboard.dto.request.VerifyPaymentRequest;
import com.finance.dashboard.dto.response.PaymentResponse;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.Payment;
import com.finance.dashboard.model.Plan;
import com.finance.dashboard.model.Subscription;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.enums.BillingCycle;
import com.finance.dashboard.model.enums.PaymentStatus;
import com.finance.dashboard.repository.PaymentRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    @Value("${razorpay.key.secret:}")
    private String razorpaySecret;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Transactional
    public Map<String, Object> createOrder(CreateOrderRequest req) {
        User user = securityUtils.getCurrentUser();
        Plan plan = planService.findBySlug(req.getPlanSlug());

        BigDecimal amount = req.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice() : plan.getMonthlyPrice();

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            Subscription sub = subscriptionService.activate(user, plan, req.getBillingCycle());
            Payment payment = paymentRepository.save(Payment.builder()
                    .user(user).plan(plan).subscription(sub)
                    .amount(BigDecimal.ZERO).status(PaymentStatus.SUCCESS)
                    .billingCycle(req.getBillingCycle().name())
                    .paidAt(LocalDateTime.now())
                    .invoiceNumber(generateInvoiceNumber())
                    .build());
            return Map.of("free", true, "subscriptionId", sub.getId());
        }

        String dummyOrderId = "order_" + System.currentTimeMillis();

        Payment pending = paymentRepository.save(Payment.builder()
                .user(user).plan(plan)
                .razorpayOrderId(dummyOrderId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .billingCycle(req.getBillingCycle().name())
                .build());

        return Map.of(
            "orderId",    dummyOrderId,
            "amount",     amount.multiply(BigDecimal.valueOf(100)).intValue(),
            "currency",   "INR",
            "keyId",      razorpayKeyId,
            "planName",   plan.getName(),
            "billingCycle", req.getBillingCycle().name()
        );
    }

    @Transactional
    public PaymentResponse verifyAndActivate(VerifyPaymentRequest req) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        if (!verifySignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed. Please contact support.");
        }

        BillingCycle cycle = BillingCycle.valueOf(payment.getBillingCycle());
        Subscription sub = subscriptionService.activate(payment.getUser(), payment.getPlan(), cycle);

        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setSubscription(sub);
        payment.setPaidAt(LocalDateTime.now());
        payment.setInvoiceNumber(generateInvoiceNumber());
        paymentRepository.save(payment);

        log.info("Payment verified and subscription activated for user: {}, plan: {}",
                payment.getUser().getUsername(), payment.getPlan().getName());

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistory(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        if (razorpaySecret == null || razorpaySecret.isBlank()) {
            log.warn("Razorpay secret not configured — skipping signature verification in dev");
            return true;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + System.currentTimeMillis() % 100000;
    }

    public PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .planName(p.getPlan() != null ? p.getPlan().getName() : "")
                .amount(p.getAmount()).currency(p.getCurrency())
                .status(p.getStatus()).billingCycle(p.getBillingCycle())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .invoiceNumber(p.getInvoiceNumber())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt())
                .build();
    }
}