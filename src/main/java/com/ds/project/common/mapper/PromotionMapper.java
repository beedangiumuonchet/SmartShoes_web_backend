package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.Promotion;
import com.ds.project.common.entities.dto.request.PromotionRequest;
import com.ds.project.common.entities.dto.response.PromotionResponse;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public Promotion mapToEntity(PromotionRequest request) {
        if (request == null) return null;
        return Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .percent(request.getPercent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? Promotion.PromotionStatus.valueOf(request.getStatus().toUpperCase()) : null)
                .build();
    }

    public PromotionResponse mapToDto(Promotion entity) {
        if (entity == null) return null;
        return PromotionResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .percent(entity.getPercent())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .build();
    }
}
