package com.ds.project.common.entities.dto;

import com.ds.project.common.enums.PaymentMethod;
import com.ds.project.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDto {
    private String id;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
