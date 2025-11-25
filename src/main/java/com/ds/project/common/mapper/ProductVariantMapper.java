package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Color;
import com.ds.project.app_context.models.Product;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.common.entities.dto.request.ProductVariantRequest;
import com.ds.project.common.entities.dto.response.ProductShortResponse;
import com.ds.project.common.entities.dto.response.ProductVariantResponse;
import com.ds.project.common.entities.dto.response.ProductVariantWithProductResponse;
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
                .colorId(variant.getColor().getId())
                .colorName(variant.getColor() != null ? variant.getColor().getName() : null)
                .size(variant.getSize())
                .price(variant.getPrice() != null ? variant.getPrice() : 0.0)
                .priceSale(variant.getPriceSale() != null ? variant.getPriceSale() : 0.0)
                .stock(variant.getStock() != null ? variant.getStock() : 0)
                .images(variant.getImages() != null
                        ? variant.getImages().stream()
                        .map(productImageMapper::mapToDto)
                        .toList()
                        : null)
                .build();
    }

    /**
     * Maps ProductVariant entity to ProductVariantWithProductResponse DTO
     */
    public ProductVariantWithProductResponse mapToDtoWithProduct(ProductVariant variant) {
        if (variant == null) return null;

        // Map phần cơ bản từ class cha
        ProductVariantResponse baseDto = mapToDto(variant);

        // Map product rút gọn
        ProductShortResponse productShort = null;
        if (variant.getProduct() != null) {
            productShort = ProductShortResponse.builder()
                    .id(variant.getProduct().getId())
                    .name(variant.getProduct().getName())
                    .build();
        }

        // Tạo instance từ baseDto (vì class con extends class cha)
        ProductVariantWithProductResponse dto = new ProductVariantWithProductResponse();
        dto.setId(baseDto.getId());
        dto.setColorName(baseDto.getColorName());
        dto.setSize(baseDto.getSize());
        dto.setPrice(baseDto.getPrice());
        dto.setStock(baseDto.getStock());
        dto.setImages(baseDto.getImages());

        // Set thêm phần riêng
        dto.setProduct(productShort);

        return dto;
    }



    /**
     * Maps a list of ProductVariant entities to list of ProductVariantResponse DTOs
     */
    public List<ProductVariantResponse> mapToDtoList(List<ProductVariant> variants) {
        if (variants == null) return List.of();
        return variants.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
