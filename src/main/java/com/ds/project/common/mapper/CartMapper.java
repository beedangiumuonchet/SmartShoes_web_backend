package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Cart;
import com.ds.project.app_context.models.CartDetail;
import com.ds.project.common.entities.dto.CartDetailDto;
import com.ds.project.common.entities.dto.CartDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto mapToDto(Cart cart) {
        if (cart == null) return null;
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .details(cart.getDetails() == null ? null :
                        cart.getDetails().stream().map(this::mapDetailToDto).collect(Collectors.toList()))
                .build();
    }

    public CartDetailDto mapDetailToDto(CartDetail detail) {
        if (detail == null) return null;
        return CartDetailDto.builder()
                .id(detail.getId())
                .productVariantId(detail.getProductVariant() != null ? detail.getProductVariant().getId() : null)
                .quantity(detail.getQuantity())
                .price(detail.getProductVariant().getPrice())
                .priceSale(detail.getProductVariant().getPriceSale())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }
}
