package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Promotion;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.models.PromotionProduct;
import com.ds.project.common.entities.dto.request.PromotionProductRequest;
import com.ds.project.common.entities.dto.response.PromotionProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionProductMapper {
    private final ProductVariantMapper productVariantMapper;

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
                .status(entity.getPromotion().getStatus()!=null ? entity.getPromotion().getStatus().name() : null)
                .promotionId(entity.getPromotion() != null ? entity.getPromotion().getId() : null)
                .productVariant(entity.getProductVariant() != null ? productVariantMapper.mapToDtoWithProduct(entity.getProductVariant()) : null)
                .build();
    }
}
