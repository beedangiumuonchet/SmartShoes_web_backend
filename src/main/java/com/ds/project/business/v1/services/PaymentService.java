package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.Payment;
import com.ds.project.app_context.repositories.OrderRepository;
import com.ds.project.app_context.repositories.PaymentRepository;
import com.ds.project.application.configs.MomoConfig;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.PaymentDto;
import com.ds.project.common.entities.dto.request.CreateMomoPaymentRequest;
import com.ds.project.common.entities.dto.request.CreatePaymentRequest;
import com.ds.project.common.entities.dto.request.MomoIpnRequest;
import com.ds.project.common.entities.dto.response.HandleMomoIpnRequest;
import com.ds.project.common.entities.dto.response.MomoPaymentResponse;
import com.ds.project.common.enums.PaymentMethod;
import com.ds.project.common.enums.PaymentStatus;
import com.ds.project.common.interfaces.IPaymentService;
import com.ds.project.common.mapper.PaymentMapper;
import com.ds.project.common.utils.HmacSHA256;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final MomoConfig momoConfig;

    // ========== CREATE GENERIC PAYMENT ==========
    @Override
    public BaseResponse<PaymentDto> createPayment(String userId, CreatePaymentRequest request) {
        try {
            // validate order exists
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // create Payment entity
            Payment p = Payment.builder()
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(Optional.ofNullable(request.getTransactionId()).orElse(UUID.randomUUID().toString()))
                    .status(PaymentStatus.PENDING)
                    .order(order)
                    .createdAt(LocalDateTime.now())
                    .build();

            Payment saved = paymentRepository.save(p);
            return BaseResponse.<PaymentDto>builder()
                    .result(Optional.of(paymentMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("createPayment error: {}", e.getMessage(), e);
            return BaseResponse.<PaymentDto>builder()
                    .message(Optional.of("Failed to create payment: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // ========== CREATE MOMO PAYMENT: trả về link và transactionId ==========
    @Override
    @Transactional
    public BaseResponse<MomoPaymentResponse> createMomoPayment(String orderId, BigDecimal amount) {
        String partnerCode = momoConfig.getPartnerCode();
        String accessKey = momoConfig.getAccessKey();
        String secretKey = momoConfig.getSecretKey();
        String ipnUrl = momoConfig.getIpnUrl();
        String returnUrl = momoConfig.getReturnUrl();
        String requestType = momoConfig.getRequestType();
        String createEndpoint = momoConfig.getCreateEndpoint();
        try {
            // ✅ 1. Tạo record Payment (PENDING)
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

            Payment payment = Payment.builder()
                    .amount(amount)
                    .paymentMethod(PaymentMethod.MOMO)
                    .transactionId(UUID.randomUUID().toString()) // orderId bên MoMo
                    .status(PaymentStatus.PENDING)
                    .order(order)
                    .build();
            paymentRepository.save(payment);
            orderRepository.save(order);

            // ✅ 2. Tạo chữ ký và request gửi MoMo
            String requestId = UUID.randomUUID().toString();
            String extraData = "";
            String orderInfo = "Thanh toán MoMo cho đơn hàng #" + orderId;

            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + payment.getTransactionId() +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = HmacSHA256.signHmacSHA256(rawSignature, secretKey);

            CreateMomoPaymentRequest momoRequest = CreateMomoPaymentRequest.builder()
                    .partnerCode(partnerCode)
                    .accessKey(accessKey)
                    .requestId(requestId)
                    .amount(String.valueOf(amount))
                    .orderId(payment.getTransactionId())
                    .orderInfo(orderInfo)
                    .redirectUrl(returnUrl)
                    .ipnUrl(ipnUrl)
                    .extraData(extraData)
                    .requestType(requestType)
                    .signature(signature)
                    .build();

            log.info("[MoMo] Sending request: {}", momoRequest);

            // ✅ 3. Gửi request
            RestTemplate restTemplate = new RestTemplate();
            MomoPaymentResponse response =
                    restTemplate.postForObject(createEndpoint, momoRequest, MomoPaymentResponse.class);

            // ✅ 4. Kiểm tra kết quả
            if (response == null || response.getResultCode() != 0) {
                log.error("[MoMo] Tạo giao dịch thất bại: {}", response);
                return BaseResponse.error("Tạo giao dịch MoMo thất bại");
            }

            log.info("[MoMo] Tạo giao dịch thành công: {}", response);
            return BaseResponse.success(response, "Tạo thanh toán MoMo thành công");

        } catch (Exception e) {
            log.error("[MoMo] Lỗi tạo giao dịch: {}", e.getMessage(), e);
            return BaseResponse.error("Lỗi tạo MoMo payment: " + e.getMessage());
        }
    }



    // ========== HANDLE MOMO IPN ==========
    @Override
    public BaseResponse<PaymentDto> handleMomoIpn(HandleMomoIpnRequest ipnRequest) {
        try {
            if (ipnRequest == null || ipnRequest.getOrderId() == null) {
                return BaseResponse.<PaymentDto>builder()
                        .message(Optional.of("Invalid ipn payload"))
                        .result(Optional.empty())
                        .build();
            }

            Optional<Payment> maybe = paymentRepository.findByTransactionId(ipnRequest.getOrderId());
            if (maybe.isEmpty()) {
                return BaseResponse.<PaymentDto>builder()
                        .message(Optional.of("Payment not found for orderId=" + ipnRequest.getOrderId()))
                        .result(Optional.empty())
                        .build();
            }

            Payment payment = maybe.get();

            // ✅ Kết quả thanh toán thành công
            if ("0".equals(ipnRequest.getResultCode())) {
                payment.setStatus(PaymentStatus.SUCCESS);
                if (payment.getOrder() != null) {
                    payment.getOrder().setStatus(com.ds.project.common.enums.OrderStatus.PAID);
                    orderRepository.save(payment.getOrder());
                }
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }

            payment.setUpdatedAt(LocalDateTime.now());
            Payment saved = paymentRepository.save(payment);

            log.info("[MoMo IPN] Updated payment {} to {}", payment.getTransactionId(), payment.getStatus());

            return BaseResponse.<PaymentDto>builder()
                    .result(Optional.of(paymentMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("handleMomoIpn error: {}", e.getMessage(), e);
            return BaseResponse.<PaymentDto>builder()
                    .message(Optional.of("Failed to process ipn: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }


    // ========== PAYMENT RETURN (user redirect after payment) ==========
    @Override
    public BaseResponse<PaymentDto> paymentReturn(String transactionId) {
        try {
            Payment payment = paymentRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Lưu ý: trong thực tế, khi user redirect bạn vẫn cần kiểm tra trạng thái thật bằng query server Momo hoặc DB (IPN)
            return BaseResponse.<PaymentDto>builder()
                    .result(Optional.of(paymentMapper.mapToDto(payment)))
                    .build();

        } catch (Exception e) {
            log.error("paymentReturn error: {}", e.getMessage(), e);
            return BaseResponse.<PaymentDto>builder()
                    .message(Optional.of("Failed on payment return: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // ========== GET ALL ==========
    @Override
    public BaseResponse<List<PaymentDto>> getAllPayments() {
        try {
            List<PaymentDto> list = paymentRepository.findAll()
                    .stream().map(paymentMapper::mapToDto)
                    .collect(Collectors.toList());
            return BaseResponse.<List<PaymentDto>>builder()
                    .result(Optional.of(list))
                    .build();
        } catch (Exception e) {
            log.error("getAllPayments error: {}", e.getMessage(), e);
            return BaseResponse.<List<PaymentDto>>builder()
                    .message(Optional.of("Failed to get payments: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // ========== GET BY ID ==========
    @Override
    public BaseResponse<PaymentDto> getPaymentById(String paymentId) {
        try {
            Payment p = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            return BaseResponse.<PaymentDto>builder()
                    .result(Optional.of(paymentMapper.mapToDto(p)))
                    .build();
        } catch (Exception e) {
            log.error("getPaymentById error: {}", e.getMessage(), e);
            return BaseResponse.<PaymentDto>builder()
                    .message(Optional.of("Failed to get payment: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }
}
