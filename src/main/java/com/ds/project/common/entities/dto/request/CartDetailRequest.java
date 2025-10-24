package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailRequest {
    @NotBlank
    private String productVariantId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
