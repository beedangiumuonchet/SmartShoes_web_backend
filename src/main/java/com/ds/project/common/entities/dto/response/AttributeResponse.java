package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * Attribute response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeResponse {
    private String id;
    private String key;
    private String value;
    private String description;
}
