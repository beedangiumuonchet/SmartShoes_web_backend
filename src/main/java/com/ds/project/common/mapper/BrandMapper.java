package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Brand;
import com.ds.project.common.entities.dto.request.BrandRequest;
import com.ds.project.common.entities.dto.response.BrandResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for Brand Entity ↔ DTO
 */
@Component
public class BrandMapper {

    public Brand toEntity(BrandRequest request) {
        if (request == null) return null;
        return Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public void updateEntity(Brand brand, BrandRequest request) {
        if (request.getName() != null) brand.setName(request.getName());
        brand.setDescription(request.getDescription());
    }

    public BrandResponse toResponse(Brand brand) {
        if (brand == null) return null;
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .build();
    }
}
