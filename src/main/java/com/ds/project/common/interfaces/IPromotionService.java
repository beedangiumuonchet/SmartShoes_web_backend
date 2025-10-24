package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.PromotionRequest;
import com.ds.project.common.entities.dto.response.PromotionResponse;

import java.util.List;

/**
 * Interface for Promotion Service
 */
public interface IPromotionService {
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(String id, PromotionRequest request);
    PromotionResponse getPromotionById(String id);
    List<PromotionResponse> getAllPromotions();
    void deletePromotion(String id);
}
