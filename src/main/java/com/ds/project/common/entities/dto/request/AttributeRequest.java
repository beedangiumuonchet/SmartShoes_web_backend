package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Attribute request DTO for create/update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeRequest {

    @NotBlank(message = "Key is required")
    private String key;

    @NotBlank(message = "Value is required")
    private String value;

    private String description;
}
