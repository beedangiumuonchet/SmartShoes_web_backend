package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ProductImage request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {

    @NotBlank(message = "URL is required")
    private String url;

    private Boolean isMain;
    private String productVariantId;
}
