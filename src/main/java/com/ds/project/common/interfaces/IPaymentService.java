package com.ds.project.common.interfaces;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.PaymentDto;
import com.ds.project.common.entities.dto.request.CreateMomoPaymentRequest;
import com.ds.project.common.entities.dto.request.CreatePaymentRequest;
import com.ds.project.common.entities.dto.request.PaymentFilterRequest;
import com.ds.project.common.entities.dto.response.HandleMomoIpnRequest;
import com.ds.project.common.entities.dto.response.MomoPaymentResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentService {
    BaseResponse<PaymentDto> createPayment(String userId, CreatePaymentRequest request);
    BaseResponse<MomoPaymentResponse> createMomoPayment(String orderId, BigDecimal amount);
    BaseResponse<PaymentDto> handleMomoIpn(HandleMomoIpnRequest ipnRequest);
    BaseResponse<PaymentDto> paymentReturn(String transactionId);
    // ========== GET ALL (phân trang + filter) ==========
    PaginationResponse<PaymentDto> getAllPayments(PaymentFilterRequest filter);

    BaseResponse<PaymentDto> getPaymentById(String paymentId);
}
