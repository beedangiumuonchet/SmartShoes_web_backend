package com.ds.project.common.mapper;

import com.ds.project.app_context.models.ProductAttribute;
import com.ds.project.common.entities.dto.response.ProductAttributeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductAttributeMapper {

    private final AttributeMapper attributeMapper; // ✅ inject mapper có sẵn

    public ProductAttributeResponse toResponse(ProductAttribute entity) {
        if (entity == null) return null;

        return ProductAttributeResponse.builder()
                .id(entity.getId())
                .attribute(attributeMapper.mapToDto(entity.getAttribute())) // ✅ map attribute sang DTO
                .build();
    }
}
