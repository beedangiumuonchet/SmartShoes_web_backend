package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * ProductAttribute request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeRequest {

    @NotBlank(message = "Attribute ID is required")
    private String attributeId;
}
