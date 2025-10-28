package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyNowRequest {
    @NotBlank
    private String productVariantId;

    @NotNull
    @Min(1)
    private Integer quantity;

    // 🆕 Thông tin giao hàng
    @NotBlank
    private String shippingName;

    @NotBlank
    private String shippingPhone;

    @NotBlank
    private String shippingAddress;
}
