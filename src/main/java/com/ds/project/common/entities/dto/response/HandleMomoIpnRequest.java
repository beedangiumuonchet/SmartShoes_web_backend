package com.ds.project.common.entities.dto.response;

import lombok.Data;

@Data
public class HandleMomoIpnRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private String orderId;
    private String transactionId;
    private String orderInfo;
    private String amount;
    private String responseTime;
    private String resultCode; // 0 = success in Momo convention
    private String signature;
}
