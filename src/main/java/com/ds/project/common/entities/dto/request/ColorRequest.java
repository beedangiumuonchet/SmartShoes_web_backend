package com.ds.project.common.entities.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorRequest {
    @NotBlank(message = "Tên màu không được để trống")
    private String name;
}
