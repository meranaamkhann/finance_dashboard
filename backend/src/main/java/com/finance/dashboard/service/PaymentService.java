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
import com.finance.dashboard.util.SecurityUtils;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
import com.finance.dashboard.model.enums.Role;
import com.finance.dashboard.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository     paymentRepository;
    private final PlanService           planService;
    private final SubscriptionService   subscriptionService;
    private final SecurityUtils         securityUtils;
    private final UserRepository        userRepository;

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpaySecret;

    @Transactional
    public Map<String, Object> createOrder(CreateOrderRequest req) {
        User user = securityUtils.getCurrentUser();
        Plan plan = planService.findBySlug(req.getPlanSlug());

        BigDecimal amount = req.getBillingCycle() == BillingCycle.YEARLY
                ? plan.getYearlyPrice() : plan.getMonthlyPrice();

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            Subscription sub = subscriptionService.activate(user, plan, req.getBillingCycle());
            paymentRepository.save(Payment.builder()
                    .user(user).plan(plan).subscription(sub)
                    .amount(BigDecimal.ZERO).status(PaymentStatus.SUCCESS)
                    .billingCycle(req.getBillingCycle().name())
                    .paidAt(LocalDateTime.now())
                    .invoiceNumber(generateInvoiceNumber())
                    .build());
            return Map.of("free", true, "subscriptionId", sub.getId());
        }

        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpaySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt",  "receipt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            Order rzpOrder = client.orders.create(orderRequest);
            String rzpOrderId = rzpOrder.get("id");

            Payment pending = paymentRepository.save(Payment.builder()
                    .user(user).plan(plan)
                    .razorpayOrderId(rzpOrderId)
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .billingCycle(req.getBillingCycle().name())
                    .build());

            log.info("Razorpay order created: {} for user: {}", rzpOrderId, user.getUsername());

            return Map.of(
                "orderId",      rzpOrderId,
                "amount",       amountInPaise,
                "currency",     "INR",
                "keyId",        razorpayKeyId,
                "planName",     plan.getName(),
                "billingCycle", req.getBillingCycle().name()
            );

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new BadRequestException("Payment initialization failed: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse verifyAndActivate(VerifyPaymentRequest req) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));

        if (!verifySignature(req.getRazorpayOrderId(),
                             req.getRazorpayPaymentId(),
                             req.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);
            throw new BadRequestException("Payment verification failed. Contact support.");
        }

        BillingCycle cycle = BillingCycle.valueOf(payment.getBillingCycle());
        Subscription sub   = subscriptionService.activate(payment.getUser(), payment.getPlan(), cycle);

        User payingUser = payment.getUser();
        Plan paidPlan   = payment.getPlan();

        if (!paidPlan.getSlug().equals("free")) {
            payingUser.setRole(Role.ANALYST);
            payingUser.setOnTrial(false);
            userRepository.save(payingUser);
            log.info("User {} upgraded to ANALYST after payment for plan: {}",
                    payingUser.getUsername(), paidPlan.getName());
        }

        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setSubscription(sub);
        payment.setPaidAt(LocalDateTime.now());
        payment.setInvoiceNumber(generateInvoiceNumber());
        paymentRepository.save(payment);

        log.info("Payment verified — user: {} plan: {}",
                payment.getUser().getUsername(), payment.getPlan().getName());

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistory(Pageable pageable) {
        return paymentRepository
                .findAllByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId(), pageable)
                .map(this::toResponse);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        if (razorpaySecret == null || razorpaySecret.isBlank()) {
            log.error("RAZORPAY SECRET NOT SET — rejecting payment");
            return false;
        }
        try {
            String payload  = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    razorpaySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            boolean valid = computed.equals(signature);
            if (!valid) log.warn("Payment signature mismatch for order: {}", orderId);
            return valid;
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String generateInvoiceNumber() {
        return "INV-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + (System.currentTimeMillis() % 100000);
    }

    public PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .planName(p.getPlan() != null ? p.getPlan().getName() : "")
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .billingCycle(p.getBillingCycle())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .invoiceNumber(p.getInvoiceNumber())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}