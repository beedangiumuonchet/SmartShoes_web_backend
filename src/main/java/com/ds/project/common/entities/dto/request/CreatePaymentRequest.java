package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.ds.project.common.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @NotNull
    private BigDecimal amount;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    private String orderId;

    // optional: transactionId nếu muốn chỉ định từ client (thường để null)
    private String transactionId;
}
