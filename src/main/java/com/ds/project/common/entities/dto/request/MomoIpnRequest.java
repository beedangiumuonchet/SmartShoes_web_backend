package com.ds.project.common.entities.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoIpnRequest {
    private String partnerCode;
    private String accessKey;
    private String amount;
    private String orderId;
    private String orderInfo;
    private String orderType;
    private String transId; // momo transaction id
    private String message;
    private int resultCode;
    private String requestId;
    private String extraData;
    private String signature;
}
