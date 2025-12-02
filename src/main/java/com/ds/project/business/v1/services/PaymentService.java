package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Order;
import com.ds.project.app_context.models.Payment;
import com.ds.project.app_context.repositories.OrderRepository;
import com.ds.project.app_context.repositories.PaymentRepository;
import com.ds.project.application.configs.MomoConfig;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.PaymentDto;
import com.ds.project.common.entities.dto.request.*;
import com.ds.project.common.entities.dto.response.HandleMomoIpnRequest;
import com.ds.project.common.entities.dto.response.MomoPaymentResponse;
import com.ds.project.common.enums.PaymentMethod;
import com.ds.project.common.enums.PaymentStatus;
import com.ds.project.common.interfaces.IPaymentService;
import com.ds.project.common.mapper.PaymentMapper;
import com.ds.project.common.utils.HmacSHA256;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
            // 1. Lấy order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

            // 2. Chỉ tạo payment nếu order.status = PENDING
            if (order.getStatus() != com.ds.project.common.enums.OrderStatus.PENDING) {
                return BaseResponse.error("Chỉ tạo payment cho đơn hàng đang PENDING");
            }

            // 3. Tạo Payment mới
            Payment payment = Payment.builder()
                    .amount(amount)
                    .paymentMethod(PaymentMethod.MOMO)
                    .transactionId(UUID.randomUUID().toString())
                    .status(PaymentStatus.PENDING)
                    .order(order)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

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

            // 3. Gửi request
            RestTemplate restTemplate = new RestTemplate();
            MomoPaymentResponse response =
                    restTemplate.postForObject(createEndpoint, momoRequest, MomoPaymentResponse.class);

            // 4. Kiểm tra kết quả
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

            // Kết quả thanh toán thành công
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

    // ========== GET ALL (phân trang + filter) ==========
    @Override
    public PaginationResponse<PaymentDto> getAllPayments(PaymentFilterRequest filter) {
        try {
            // ====== Cấu hình sắp xếp ======
            String sortBy = (filter.getSortBy() != null && !filter.getSortBy().isBlank())
                    ? filter.getSortBy() : "createdAt";
            String sortDir = (filter.getSortDirection() != null && !filter.getSortDirection().isBlank())
                    ? filter.getSortDirection() : "desc";

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            // ====== Cấu hình phân trang ======
            int page = (filter.getPage() != null && filter.getPage() >= 0) ? filter.getPage() : 0;
            int size = (filter.getSize() != null && filter.getSize() > 0) ? filter.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, sort);

            // ====== Xây dựng điều kiện filter ======
            Specification<Payment> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Tìm kiếm theo id, transactionCode hoặc username
                if (filter.getQ() != null && !filter.getQ().isBlank()) {
                    String kw = "%" + filter.getQ().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("id").as(String.class)), kw),
                            cb.like(cb.lower(root.get("transactionId")), kw)
                            //cb.like(cb.lower(root.get("user").get("username")), kw)
                    ));
                }

                // Lọc theo status
                if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                // Lọc theo method
                if (filter.getMethod() != null && !filter.getMethod().isBlank()) {
                    predicates.add(cb.equal(root.get("method"), filter.getMethod()));
                }

                // Lọc theo ngày tạo
                if (filter.getCreatedDate_from() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                            root.get("createdAt"), filter.getCreatedDate_from().atStartOfDay()
                    ));
                }

                if (filter.getCreatedDate_to() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                            root.get("createdAt"), filter.getCreatedDate_to().atTime(23, 59, 59)
                    ));
                }

                // Lọc theo userId (nếu có)
                if (filter.getUserId() != null && !filter.getUserId().isBlank()) {
                    predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            // ====== Truy vấn & map kết quả ======
            var pageResult = paymentRepository.findAll(spec, pageable);
            List<PaymentDto> content = pageResult.getContent().stream()
                    .map(paymentMapper::mapToDto)
                    .toList();

            log.info("✅ getAllPayments fetched {} records (page {} / {})",
                    content.size(), pageResult.getNumber() + 1, pageResult.getTotalPages());

            // ====== Trả về kết quả phân trang ======
            return PaginationResponse.<PaymentDto>builder()
                    .content(content)
                    .page(pageResult.getNumber())
                    .size(pageResult.getSize())
                    .totalElements(pageResult.getTotalElements())
                    .totalPages(pageResult.getTotalPages())
                    .first(pageResult.isFirst())
                    .last(pageResult.isLast())
                    .hasNext(pageResult.hasNext())
                    .hasPrevious(pageResult.hasPrevious())
                    .build();

        } catch (Exception e) {
            log.error("❌ getAllPayments error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch payments: " + e.getMessage(), e);
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

    @Override
    public BaseResponse<List<PaymentDto>> getPaymentsByOrderId(String orderId) {
        try {
            // kiểm tra order tồn tại
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // lấy tất cả payment thuộc order
            List<Payment> payments = paymentRepository.findByOrderId(orderId);

            List<PaymentDto> dtos = payments.stream()
                    .map(paymentMapper::mapToDto)
                    .toList();

            return BaseResponse.<List<PaymentDto>>builder()
                    .result(Optional.of(dtos))
                    .build();

        } catch (Exception e) {
            log.error("Error in getPaymentsByOrderId: {}", e.getMessage(), e);

            return BaseResponse.<List<PaymentDto>>builder()
                    .message(Optional.of("Failed to get payments: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

}
