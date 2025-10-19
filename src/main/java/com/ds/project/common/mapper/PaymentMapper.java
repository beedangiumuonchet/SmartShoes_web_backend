package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Payment;
import com.ds.project.common.entities.dto.PaymentDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDto mapToDto(Payment p) {
        if (p == null) return null;
        return PaymentDto.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .status(p.getStatus())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
