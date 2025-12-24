package com.ds.project.application.controllers.v1;

import com.ds.project.app_context.models.Payment;
import com.ds.project.app_context.repositories.OrderRepository;
import com.ds.project.app_context.repositories.PaymentRepository;
import com.ds.project.application.annotations.AuthRequired;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.common.UserPayload;
import com.ds.project.common.entities.dto.PaymentDto;
import com.ds.project.common.entities.dto.request.CreateMomoPaymentRequest;
import com.ds.project.common.entities.dto.request.CreatePaymentRequest;
import com.ds.project.common.entities.dto.request.PaymentFilterRequest;
import com.ds.project.common.entities.dto.response.HandleMomoIpnRequest;
import com.ds.project.common.entities.dto.response.MomoPaymentResponse;
import com.ds.project.common.enums.PaymentStatus;
import com.ds.project.common.interfaces.IPaymentService;
import com.ds.project.common.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPayload payload) {
            return payload.getUserId();
        }
        return null;
    }

    @AuthRequired
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseUtils.error("Unauthorized", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        BaseResponse<PaymentDto> resp = paymentService.createPayment(userId, request);
        return resp.getResult().isPresent()
                ? ResponseUtils.success(resp.getResult().get(), "Payment created")
                : ResponseUtils.error(resp.getMessage().orElse("Failed to create payment"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/momo")
    public ResponseEntity<Map<String, Object>> createMomoPayment(
            @RequestParam String orderId,
            @RequestParam BigDecimal amount
    ) {
        try {
            BaseResponse<MomoPaymentResponse> resp = paymentService.createMomoPayment(orderId, amount);

            return resp.getResult().isPresent()
                    ? ResponseUtils.success(resp.getResult().get(), "Momo payment created")
                    : ResponseUtils.error(
                    resp.getMessage().orElse("Failed to create momo payment"),
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return ResponseUtils.error(
                    "Internal server error: " + e.getMessage(),
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }



    /**
     * Momo IPN endpoint - Momo sẽ gọi endpoint này (không cần auth)
     * Thông thường IPN sẽ POST JSON
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<Map<String, Object>> handleMomoIpn(@RequestBody HandleMomoIpnRequest ipn) {
        BaseResponse<PaymentDto> resp = paymentService.handleMomoIpn(ipn);
        // Momo expects HTTP 200 and sometimes specific body; chúng ta trả về success/fail thông dụng
        return resp.getResult().isPresent()
                ? ResponseUtils.success(resp.getResult().get(), "IPN processed")
                : ResponseUtils.error(resp.getMessage().orElse("Failed to process ipn"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    /**
     * User return after Momo payment (redirect). Dùng query param transactionId
     */
    @GetMapping("/momo/return")
    public ResponseEntity<Map<String, Object>> paymentReturn(@RequestParam("transactionId") String transactionId) {
        try {
            Payment payment = paymentRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            String redirectUrl;
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                // Redirect đến trang chi tiết order
                redirectUrl = "http://localhost:5173/orders/" + payment.getOrder().getId();
            } else {
                // Redirect đến trang thất bại
                redirectUrl = "http://localhost:5173/payments/payment-failed";
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } catch (Exception e) {
            log.error("paymentReturn error: {}", e.getMessage(), e);
            // Redirect về trang thất bại nếu có lỗi
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "http://localhost:5173/payments/payment-failed")
                    .build();
        }
    }

    @AuthRequired
    @GetMapping
    public ResponseEntity<?> getAllPayments(@ModelAttribute PaymentFilterRequest filter) {
        try {
            PaginationResponse<PaymentDto> response = paymentService.getAllPayments(filter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch payments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch payments: " + e.getMessage());
        }
    }

    @AuthRequired
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable String paymentId) {
        BaseResponse<PaymentDto> resp = paymentService.getPaymentById(paymentId);
        return resp.getResult().isPresent()
                ? ResponseUtils.success(resp.getResult().get())
                : ResponseUtils.error(resp.getMessage().orElse("Failed to get payment"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<Map<String, Object>> getPaymentsByOrderId(@PathVariable String orderId) {

        BaseResponse<List<PaymentDto>> response = paymentService.getPaymentsByOrderId(orderId);

        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get())
                : ResponseUtils.error(response.getMessage().orElse("Failed to get payments"),
                HttpStatus.BAD_REQUEST);
    }

}
