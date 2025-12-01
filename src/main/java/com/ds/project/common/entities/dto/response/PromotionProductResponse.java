package com.ds.project.common.entities.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionProductResponse {
    private String id;
    private String promotionId;
    private String status;
    private ProductVariantWithProductResponse productVariant;
}
