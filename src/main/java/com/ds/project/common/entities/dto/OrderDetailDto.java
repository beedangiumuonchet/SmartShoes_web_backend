package com.ds.project.common.entities.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDto {
    private String id;
    private String productVariantId;
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private String sku;
    private String variantInfo; // color/size đảo ra chuỗi nếu cần
}
