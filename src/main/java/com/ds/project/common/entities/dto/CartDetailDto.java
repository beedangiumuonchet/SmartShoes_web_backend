package com.ds.project.common.entities.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailDto {
    private String id;
    private String productVariantId;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
