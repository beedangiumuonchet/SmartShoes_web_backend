package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Promotion;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.models.PromotionProduct;
import com.ds.project.common.entities.dto.request.PromotionProductRequest;
import com.ds.project.common.entities.dto.response.PromotionProductResponse;
import org.springframework.stereotype.Component;

@Component
public class PromotionProductMapper {

    public PromotionProduct mapToEntity(PromotionProductRequest request, Promotion promotion, ProductVariant variant) {
        if (request == null) return null;
        return PromotionProduct.builder()
                .promotion(promotion)
                .productVariant(variant)
                .build();
    }

    public PromotionProductResponse mapToDto(PromotionProduct entity) {
        if (entity == null) return null;
        return PromotionProductResponse.builder()
                .id(entity.getId())
                .promotionId(entity.getPromotion() != null ? entity.getPromotion().getId() : null)
                .productVariantId(entity.getProductVariant() != null ? entity.getProductVariant().getId() : null)
                .build();
    }
}
