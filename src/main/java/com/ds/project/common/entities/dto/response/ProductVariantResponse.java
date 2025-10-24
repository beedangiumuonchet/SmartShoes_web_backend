package com.ds.project.common.entities.dto.response;

import com.ds.project.app_context.models.Color;
import lombok.*;

import java.util.List;

/**
 * ProductVariant response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private String id;
    private String colorName;
    private String size;
    private Double price;
    private Integer stock;
    // 🔹 Danh sách ảnh của variant
    private List<ProductImageResponse> images;
}
