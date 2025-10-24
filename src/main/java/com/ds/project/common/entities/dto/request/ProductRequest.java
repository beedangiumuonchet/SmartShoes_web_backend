package com.ds.project.common.entities.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String description;
    private String status;

    @NotBlank(message = "Cần chọn thương hiệu")
    private String brandId;

    @NotBlank(message = "Cần chọn danh mục")
    private String categoryId;

    // Danh sách variant (màu, size, giá, tồn kho)
    private List<ProductVariantRequest> variants;

    // Danh sách ảnh sản phẩm
//    private List<ProductImageRequest> images;

    // Danh sách thuộc tính
    private List<ProductAttributeRequest> attributes;
}
