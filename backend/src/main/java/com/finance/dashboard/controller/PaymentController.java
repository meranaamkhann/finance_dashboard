package com.finance.dashboard.controller;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    private final SecurityUtils securityUtils;

    @GetMapping("/config")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> getConfig() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "keyId", razorpayKeyId,
            "currency", "INR"
        )));
    }

    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> verify(
            @RequestBody Map<String, String> payload) {
        String orderId   = payload.get("razorpay_order_id");
        String paymentId = payload.get("razorpay_payment_id");
        String signature = payload.get("razorpay_signature");

        log.info("Payment verification: orderId={} paymentId={} user={}",
                orderId, paymentId, securityUtils.getCurrentUsername());

        return ResponseEntity.ok(ApiResponse.ok("Payment received. Plan activation coming soon.",
                Map.of("paymentId", paymentId, "status", "success")));
    }
}