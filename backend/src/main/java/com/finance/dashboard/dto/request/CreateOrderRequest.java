package com.finance.dashboard.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String planId;
    @NotNull
    private Integer amountInPaise;
}