package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * Brand response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {
    private String id;
    private String name;
    private String description;
    private String url;
    private String slug;
}
