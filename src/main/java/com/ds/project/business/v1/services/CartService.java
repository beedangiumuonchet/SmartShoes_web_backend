package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Cart;
import com.ds.project.app_context.models.CartDetail;
import com.ds.project.app_context.models.ProductVariant;
import com.ds.project.app_context.models.User;
import com.ds.project.app_context.repositories.CartDetailRepository;
import com.ds.project.app_context.repositories.CartRepository;
import com.ds.project.app_context.repositories.ProductVariantRepository;
import com.ds.project.app_context.repositories.UserRepository;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.CartDetailDto;
import com.ds.project.common.entities.dto.CartDto;
import com.ds.project.common.entities.dto.request.CartDetailRequest;
import com.ds.project.common.interfaces.ICartService;
import com.ds.project.common.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;

    @Override
    public BaseResponse<CartDto> getUserCart(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Cart> cartOpt = cartRepository.findByUser(user);
            if (cartOpt.isEmpty()) {
                return BaseResponse.<CartDto>builder()
                        .result(Optional.empty())
                        .message(Optional.of("Cart not found"))
                        .build();
            }

            return BaseResponse.<CartDto>builder()
                    .result(Optional.of(cartMapper.mapToDto(cartOpt.get())))
                    .build();
        } catch (Exception e) {
            log.error("Error getting cart for user {}: {}", userId, e.getMessage(), e);
            return BaseResponse.<CartDto>builder()
                    .message(Optional.of("Failed to get cart: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public BaseResponse<CartDto> createCart(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Optional<Cart> existing = cartRepository.findByUser(user);
            if (existing.isPresent()) {
                return BaseResponse.<CartDto>builder()
                        .result(Optional.of(cartMapper.mapToDto(existing.get())))
                        .build();
            }

            Cart cart = Cart.builder()
                    .user(user)
                    .total(0.0)
                    .build();

            Cart saved = cartRepository.save(cart);
            return BaseResponse.<CartDto>builder()
                    .result(Optional.of(cartMapper.mapToDto(saved)))
                    .build();
        } catch (Exception e) {
            log.error("Error creating cart for user {}: {}", userId, e.getMessage(), e);
            return BaseResponse.<CartDto>builder()
                    .message(Optional.of("Failed to create cart: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public BaseResponse<CartDetailDto> addCartDetail(String cartId, CartDetailRequest request) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("ProductVariant not found"));

            // check existing detail
            Optional<CartDetail> existingOpt = cartDetailRepository.findByCartAndProductVariant(cart, variant);
            CartDetail detail;
            if (existingOpt.isPresent()) {
                detail = existingOpt.get();
                detail.setQuantity(detail.getQuantity() + request.getQuantity());
                detail.setSubtotal(detail.getPrice() * detail.getQuantity());
                detail.setUpdatedAt(LocalDateTime.now());
            } else {
                double unitPrice = variant.getPrice() != null ? variant.getPrice() : 0.0;
                detail = CartDetail.builder()
                        .cart(cart)
                        .productVariant(variant)
                        .quantity(request.getQuantity())
                        .price(unitPrice)
                        .subtotal(unitPrice * request.getQuantity())
                        .build();
                cart.getDetails().add(detail);
            }

            // recalc cart total
            double total = cart.getDetails().stream().mapToDouble(CartDetail::getSubtotal).sum();
            cart.setTotal(total);
            cart.setUpdatedAt(LocalDateTime.now());

            cartRepository.save(cart); // cascade will save details
            return BaseResponse.<CartDetailDto>builder()
                    .result(Optional.of(cartMapper.mapDetailToDto(detail)))
                    .build();
        } catch (Exception e) {
            log.error("Error adding cart detail to cart {}: {}", cartId, e.getMessage(), e);
            return BaseResponse.<CartDetailDto>builder()
                    .message(Optional.of("Failed to add cart detail: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public BaseResponse<CartDetailDto> updateCartDetail(String detailId, CartDetailRequest request) {
        try {
            CartDetail detail = cartDetailRepository.findById(detailId)
                    .orElseThrow(() -> new RuntimeException("CartDetail not found"));

            // optionally validate productVariant id matches (if provided)
            if (request.getProductVariantId() != null && !request.getProductVariantId().equals(detail.getProductVariant().getId())) {
                ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                        .orElseThrow(() -> new RuntimeException("ProductVariant not found"));
                detail.setProductVariant(variant);
                detail.setPrice(variant.getPrice() != null ? variant.getPrice() : detail.getPrice());
            }

            detail.setQuantity(request.getQuantity());
            detail.setSubtotal(detail.getPrice() * detail.getQuantity());
            detail.setUpdatedAt(LocalDateTime.now());
            cartDetailRepository.save(detail);

            // update cart total
            Cart cart = detail.getCart();
            double total = cart.getDetails().stream().mapToDouble(CartDetail::getSubtotal).sum();
            cart.setTotal(total);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);

            return BaseResponse.<CartDetailDto>builder()
                    .result(Optional.of(cartMapper.mapDetailToDto(detail)))
                    .build();
        } catch (Exception e) {
            log.error("Error updating cart detail {}: {}", detailId, e.getMessage(), e);
            return BaseResponse.<CartDetailDto>builder()
                    .message(Optional.of("Failed to update cart detail: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public void deleteCartDetail(String detailId) {
        try {
            CartDetail detail = cartDetailRepository.findById(detailId)
                    .orElseThrow(() -> new RuntimeException("CartDetail not found"));
            Cart cart = detail.getCart();
            cart.getDetails().remove(detail);
            cart.setUpdatedAt(LocalDateTime.now());
            // recalc total
            double total = cart.getDetails().stream().mapToDouble(CartDetail::getSubtotal).sum();
            cart.setTotal(total);
            cartRepository.save(cart); // will remove detail due to orphanRemoval = true
            log.info("Deleted cart detail {}", detailId);
        } catch (Exception e) {
            log.error("Error deleting cart detail {}: {}", detailId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart detail: " + e.getMessage());
        }
    }

    @Override
    public void clearCart(String cartId) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            cart.getDetails().clear();
            cart.setTotal(0.0);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            log.info("Cleared cart {}", cartId);
        } catch (Exception e) {
            log.error("Error clearing cart {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to clear cart: " + e.getMessage());
        }
    }
}
