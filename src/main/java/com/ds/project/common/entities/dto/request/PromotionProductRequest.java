package com.ds.project.common.entities.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionProductRequest {
    private String promotionId;
    private String productVariantId;
}
