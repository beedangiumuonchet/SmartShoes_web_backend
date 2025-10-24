package com.ds.project.common.entities.dto.response;

import lombok.*;

/**
 * Color response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorResponse {
    private String id;
    private String name;
}
