package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.PromotionProductRequest;
import com.ds.project.common.entities.dto.response.PromotionProductResponse;

import java.util.List;

/**
 * Interface for PromotionProduct Service
 */
public interface IPromotionProductService {
    PromotionProductResponse create(PromotionProductRequest request);
    PromotionProductResponse getById(String id);
    List<PromotionProductResponse> getAll();
    List<PromotionProductResponse> getByPromotionId(String promotionId);
    List<PromotionProductResponse> getByProductVariantId(String productVariantId);
    void delete(String id);
}
