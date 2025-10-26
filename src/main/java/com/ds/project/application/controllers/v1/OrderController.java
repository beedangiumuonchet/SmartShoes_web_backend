package com.ds.project.application.controllers.v1;

import com.ds.project.application.annotations.AuthRequired;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.common.UserPayload;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.request.BuyNowRequest;
import com.ds.project.common.entities.dto.request.FromCartRequest;
import com.ds.project.common.entities.dto.request.OrderFilterRequest;
import com.ds.project.common.entities.dto.request.UpdateStatusRequest;
import com.ds.project.common.interfaces.IOrderService;
import com.ds.project.common.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    /**
     * Lấy userId từ SecurityContext (đã có trong JWT)
     */
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPayload payload) {
            System.out.println("Authenticated userId = " + payload.getUserId());

            return payload.getUserId(); // ✅ Lấy đúng userId từ JWT
        }
        return null;
    }


    @AuthRequired
    @PostMapping("/buy-now")
    public ResponseEntity<Map<String, Object>> buyNow(@Valid @RequestBody BuyNowRequest buyNowRequest) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseUtils.error("Unauthorized - user not authenticated", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        System.out.println(">>> Auth name: " + getCurrentUserId());

        BaseResponse<OrderDto> response = orderService.buyNow(userId, buyNowRequest);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get(), "Order created successfully")
                : ResponseUtils.error(response.getMessage().orElse("Failed to create order"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @PostMapping("/from-cart")
    public ResponseEntity<Map<String, Object>> fromCart(@Valid @RequestBody FromCartRequest cartRequest) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return ResponseUtils.error("Unauthorized - user not authenticated", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        BaseResponse<OrderDto> response = orderService.fromCart(userId, cartRequest);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get(), "Order created from cart successfully")
                : ResponseUtils.error(response.getMessage().orElse("Failed to create order from cart"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @GetMapping
    public ResponseEntity<?> getAllOrders(@ModelAttribute OrderFilterRequest filter) {
        try {
            PaginationResponse<OrderDto> response = orderService.getAllOrders(filter);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to fetch orders: " + e.getMessage());
        }
    }


    @AuthRequired
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable String userId) {
        BaseResponse<List<OrderDto>> response = orderService.getOrdersByUser(userId);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get())
                : ResponseUtils.error(response.getMessage().orElse("Failed to get user orders"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        BaseResponse<OrderDto> response = orderService.getOrderById(orderId);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get())
                : ResponseUtils.error(response.getMessage().orElse("Failed to get order"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateStatusRequest statusRequest) {

        BaseResponse<OrderDto> response = orderService.updateStatus(orderId, statusRequest);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get(), "Order status updated successfully")
                : ResponseUtils.error(response.getMessage().orElse("Failed to update status"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @AuthRequired
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderId) {
        BaseResponse<OrderDto> response = orderService.cancelOrder(orderId);
        return response.getResult().isPresent()
                ? ResponseUtils.success(response.getResult().get(), "Order cancelled successfully")
                : ResponseUtils.error(response.getMessage().orElse("Failed to cancel order"),
                org.springframework.http.HttpStatus.BAD_REQUEST);
    }
}
