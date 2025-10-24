package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * ProductAttribute response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeResponse {
    private String id;
    private AttributeResponse attribute;
}
