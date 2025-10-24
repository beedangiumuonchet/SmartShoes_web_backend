package com.ds.project.common.mapper;

import com.ds.project.app_context.models.*;
import com.ds.project.common.entities.dto.request.*;
import com.ds.project.common.entities.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Product entity and DTOs
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final AttributeMapper attributeMapper;
    private final ProductAttributeMapper productAttributeMapper;
    private final ProductVariantMapper productVariantMapper;

    /**
     * Maps ProductRequestDTO to Product entity
     */
    public Product mapToEntity(ProductRequest request, Brand brand, Category category,
                               List<ProductVariant> variants,
                               List<ProductImage> images,
                               List<ProductAttribute> attributes,
                               String slug) {

        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? Product.Status.valueOf(request.getStatus().toUpperCase()) : null) // ✅ String → Enum
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .brand(brand)
                .category(category)
                .variants(variants != null ? variants.stream().collect(Collectors.toSet()) : null)
//                .images(images != null ? images.stream().collect(Collectors.toSet()) : null)
                .productAttributes(attributes != null ? attributes.stream().collect(Collectors.toSet()) : null)
                .build();
    }

    /**
     * Maps Product entity to ProductResponse DTO
     */
    public ProductResponse mapToDto(Product product) {
        List<ProductVariantResponse> variantResponses = List.of();
        List<ProductImageResponse> imageResponses = List.of();
        List<ProductAttributeResponse> attributeResponses = List.of();

        try {
            // map variants
            if (product.getVariants() != null) {
                variantResponses = product.getVariants().stream()
                        .map(productVariantMapper::mapToDto)
                        .collect(Collectors.toList());
            }

//            if (product.getImages() != null) {
//                imageResponses = product.getImages().stream()
//                        .map(img -> ProductImageResponse.builder()
//                                .id(img.getId())
//                                .url(img.getUrl())
//                                .isMain(img.getIsMain())
//                                .build())
//                        .collect(Collectors.toList());
//            }

            // map productAttributes
            if (product.getProductAttributes() != null) {
                attributeResponses = product.getProductAttributes().stream()
                        .map(productAttributeMapper::toResponse)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ProductMapper.class)
                    .warn("Failed to map product nested fields for product {}: {}", product.getId(), e.getMessage());
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .slug(product.getSlug())
                .createdAt(product.getCreatedAt())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .variants(variantResponses)
//                .images(imageResponses)
                .attributes(attributeResponses)
                .build();
    }
}
