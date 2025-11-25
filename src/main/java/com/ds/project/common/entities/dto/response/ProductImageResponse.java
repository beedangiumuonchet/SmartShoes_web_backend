package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * ProductImage response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageResponse {
    private String id;
    private String url;
    private Boolean isMain;
    private String productVariantId;
//    private Double[] embedding;
}
