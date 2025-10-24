package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Color;
import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.common.entities.dto.request.ProductVariantRequest;
import com.ds.project.common.entities.dto.response.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ProductVariant entity and DTOs
 */
@Component
@RequiredArgsConstructor
public class ProductVariantMapper {

    private final ProductImageMapper productImageMapper;
    /**
     * Maps ProductVariantRequest DTO to ProductVariant entity
     */
    public ProductVariant mapToEntity(ProductVariantRequest request, Product product, Color color) {
        return ProductVariant.builder()
                .product(product)
                .color(color)
                .size(request.getSize())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
    }

    /**
     * Maps ProductVariant entity to ProductVariantResponse DTO
     */
    public ProductVariantResponse mapToDto(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .size(variant.getSize())
                .price(variant.getPrice() != null ? variant.getPrice() : 0.0)
                .stock(variant.getStock() != null ? variant.getStock() : 0)
                .images(variant.getImages() != null
                        ? variant.getImages().stream()
                        .map(productImageMapper::mapToDto)
                        .toList()
                        : null)
                .build();
    }

    /**
     * Maps a list of ProductVariant entities to list of ProductVariantResponse DTOs
     */
    public List<ProductVariantResponse> mapToDtoList(List<ProductVariant> variants) {
        if (variants == null) return List.of();
        return variants.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
