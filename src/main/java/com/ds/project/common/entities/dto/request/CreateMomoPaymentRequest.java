package com.ds.project.common.entities.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomoPaymentRequest {
    private String partnerCode;   // ✅ Bắt buộc
    private String accessKey;     // ✅ Bắt buộc
    private String requestId;     // ✅ Bắt buộc
    private String amount;        // ✅ Bắt buộc
    private String orderId;       // ✅ Bắt buộc
    private String orderInfo;     // ✅ Bắt buộc
    private String redirectUrl;   // ✅ Bắt buộc
    private String ipnUrl;        // ✅ Bắt buộc
    private String extraData;     // ✅ Có thể rỗng
    private String requestType;   // ✅ payWithMethod
    private String signature;
}
