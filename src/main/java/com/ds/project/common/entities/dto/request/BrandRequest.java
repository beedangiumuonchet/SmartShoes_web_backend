package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Brand request DTO for create/update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    private String name;

    private String description;
}
