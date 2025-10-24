package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

/**
 * ProductVariant request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "Color ID is required")
    private String colorId;

    @NotBlank(message = "Size is required")
    private String size;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    @Positive(message = "Stock must be greater than 0")
    private Integer stock;

    // Danh sách ảnh sản phẩm
    private List<ProductImageRequest> images;
}
