package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * Category response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String name;
    private String description;
}
