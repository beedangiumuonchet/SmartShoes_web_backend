package com.ds.project.common.entities.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private String status;
    private String slug;
    private LocalDateTime createdAt;

    private BrandResponse brand;
    private CategoryResponse category;

//    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    private List<ProductAttributeResponse> attributes;
}
