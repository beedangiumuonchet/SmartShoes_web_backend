package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ProductImage request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {

//    @NotBlank(message = "URL is required")
//    private String url;

    private String id;
    private Boolean isMain;

    private MultipartFile file;

    private float[] embedding;

    private String productVariantId;
}
