package com.finance.dashboard.controller;
import com.finance.dashboard.dto.request.CreateOrderRequest;
import com.finance.dashboard.dto.request.VerifyPaymentRequest;
import com.finance.dashboard.dto.response.ApiResponse;
import com.finance.dashboard.dto.response.PagedResponse;
import com.finance.dashboard.dto.response.PaymentResponse;
import com.finance.dashboard.dto.response.SubscriptionResponse;
import com.finance.dashboard.service.PaymentService;
import com.finance.dashboard.service.SubscriptionService;
import com.finance.dashboard.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import com.finance.dashboard.service.InvoiceService;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Billing", description = "Payments, subscriptions and invoices")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;
    private final InvoiceService invoiceService;

    @PostMapping("/orders")
    @Operation(summary = "Create Razorpay order for a plan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createOrder(req)));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature and activate subscription")
    public ResponseEntity<ApiResponse<PaymentResponse>> verify(
            @Valid @RequestBody VerifyPaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and subscription activated",
                paymentService.verifyAndActivate(req)));
    }

    @GetMapping("/payments")
    @Operation(summary = "My payment history")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = paymentService.getHistory(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.ok(new PagedResponse<>(result)));
    }

    @GetMapping("/subscription")
    @Operation(summary = "My current active subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription() {
        Long uid = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getCurrentSubscription(uid)));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "My full subscription history")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory() {
        Long uid = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getHistory(uid)));
    }
     
    @GetMapping("/payments/{id}/invoice")
    @Operation(summary = "Download PDF invoice for a payment")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        byte[] pdf = invoiceService.generateInvoicePdf(id);
        String filename = "invoice-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel current subscription")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @RequestParam(required = false, defaultValue = "") String reason) {
        subscriptionService.cancel(securityUtils.getCurrentUserId(), reason);
        return ResponseEntity.ok(ApiResponse.ok("Subscription cancelled", null));
    }
}