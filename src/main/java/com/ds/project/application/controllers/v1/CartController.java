package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.CartDto;
import com.ds.project.common.entities.dto.CartDetailDto;
import com.ds.project.common.entities.dto.request.CartDetailRequest;
import com.ds.project.common.interfaces.ICartService;
import com.ds.project.common.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserCart(@PathVariable String userId) {
        BaseResponse<CartDto> response = cartService.getUserCart(userId);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to get cart"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    //tạo giỏ hàng by userId
    @PostMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> createCart(@PathVariable String userId) {
        BaseResponse<CartDto> response = cartService.createCart(userId);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to create cart"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    //thêm sản phẩm
    @PostMapping("/{cartId}/details")
    public ResponseEntity<Map<String, Object>> addCartDetail(
            @PathVariable String cartId,
            @Valid @RequestBody CartDetailRequest request) {
        BaseResponse<CartDetailDto> response = cartService.addCartDetail(cartId, request);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to add cart detail"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    //Cập nhật số lượng sản phẩm
    @PutMapping("/details/{detailId}")
    public ResponseEntity<Map<String, Object>> updateCartDetail(
            @PathVariable String detailId,
            @Valid @RequestBody CartDetailRequest request) {
        BaseResponse<CartDetailDto> response = cartService.updateCartDetail(detailId, request);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to update cart detail"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    //Xóa 1 sản phẩm khỏi giỏ
    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<Map<String, Object>> deleteCartDetail(@PathVariable String detailId) {
        try {
            cartService.deleteCartDetail(detailId);
            return ResponseUtils.success(null, "Cart detail deleted successfully");
        } catch (Exception e) {
            return ResponseUtils.error("Failed to delete cart detail: " + e.getMessage(),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    //Xóa toàn bộ giỏ hàng
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Map<String, Object>> clearCart(@PathVariable String cartId) {
        try {
            cartService.clearCart(cartId);
            return ResponseUtils.success(null, "Cart cleared successfully");
        } catch (Exception e) {
            return ResponseUtils.error("Failed to clear cart: " + e.getMessage(),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}
