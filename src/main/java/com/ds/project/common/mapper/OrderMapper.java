package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.OrderDetail;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.OrderDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    public OrderDto mapToDto(Order order) {
        if (order == null) return null;

        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .total_amount(order.getTotalAmount())
                .shippingName(order.getShippingName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderDetails(order.getOrderDetails() != null
                        ? order.getOrderDetails().stream().map(this::mapDetailToDto).collect(Collectors.toList())
                        : null)
                .build();
    }

    public OrderDetailDto mapDetailToDto(OrderDetail detail) {
        if (detail == null) return null;
        OrderDetailDto dto = OrderDetailDto.builder()
                .id(detail.getId())
                .productVariantId(detail.getProductVariant() != null ? detail.getProductVariant().getId() : null)
                .productId(detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null
                        ? detail.getProductVariant().getProduct().getId() : null)
                .productName(detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null
                        ? detail.getProductVariant().getProduct().getName() : null)
                .quantity(detail.getQuantity())
                .price(detail.getPrice())
                .subtotal(detail.getSubtotal())
                .variantInfo(detail.getProductVariant() != null
                        ? (detail.getProductVariant().getColor() + " / " + detail.getProductVariant().getSize())
                        : null)
                .build();
        return dto;
    }
}
