package com.ds.project.common.mapper;

import com.ds.project.app_context.models.ProductImage;
import com.ds.project.common.entities.dto.request.ProductImageRequest;
import com.ds.project.common.entities.dto.response.ProductImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductImageMapper {

    public ProductImage mapToEntity(ProductImageRequest request) {
        if (request == null) return null;
        return ProductImage.builder()
                .isMain(request.getIsMain())
                .build();
    }

    public ProductImageResponse mapToDto(ProductImage entity) {
        if (entity == null) return null;
        return ProductImageResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .isMain(entity.getIsMain())
                .build();
    }
}
