package com.finance.dashboard.dto.response;

import com.finance.dashboard.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class PaymentResponse {
    private Long id;
    private String planName;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String billingCycle;
    private String razorpayPaymentId;
    private String invoiceNumber;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}