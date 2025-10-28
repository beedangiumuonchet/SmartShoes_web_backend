package com.ds.project.common.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FromCartRequest {
    @NotBlank
    private String cartId; // tạo từ giỏ hàng này (thường lấy theo user)

    // 🆕 Thông tin giao hàng
    @NotBlank
    private String shippingName;

    @NotBlank
    private String shippingPhone;

    @NotBlank
    private String shippingAddress;
}
