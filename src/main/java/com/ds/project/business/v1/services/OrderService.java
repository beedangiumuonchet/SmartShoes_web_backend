package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.request.BuyNowRequest;
import com.ds.project.common.entities.dto.request.FromCartRequest;
import com.ds.project.common.entities.dto.request.UpdateStatusRequest;
import com.ds.project.common.enums.OrderStatus;
import com.ds.project.common.interfaces.IOrderService;
import com.ds.project.common.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final OrderMapper orderMapper;

    // =============== BUY NOW ===============
    @Override
    public BaseResponse<OrderDto> buyNow(String userId, BuyNowRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));

            if (variant.getStock() == null || variant.getStock() < request.getQuantity()) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Số lượng vượt quá tồn kho"))
                        .result(Optional.empty())
                        .build();
            }

            double price = variant.getPrice();
            int quantity = request.getQuantity();
            double subtotal = price * quantity;

            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .totalAmount(subtotal)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(quantity)
                    .price(price)
                    .subtotal(subtotal)
                    .build();

            order.setOrderDetails(Collections.singletonList(detail));

            // Save order (cascade persist details)
            Order saved = orderRepository.save(order);

            // Update stock
            variant.setStock(variant.getStock() - quantity);
            productVariantRepository.save(variant);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("Error in buyNow: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to create order: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== FROM CART ===============
    @Override
    public BaseResponse<OrderDto> fromCart(String userId, FromCartRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Cart cart = cartRepository.findById(request.getCartId())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);
            if (cartDetails == null || cartDetails.isEmpty()) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Giỏ hàng trống"))
                        .result(Optional.empty())
                        .build();
            }

            // Check stock
            for (CartDetail cd : cartDetails) {
                ProductVariant pv = cd.getProductVariant();
                if (pv.getStock() == null || pv.getStock() < cd.getQuantity()) {
                    return BaseResponse.<OrderDto>builder()
                            .message(Optional.of("Sản phẩm " +
                                    (pv.getProduct() != null ? pv.getProduct().getName() : "") +
                                    " vượt quá tồn kho"))
                            .result(Optional.empty())
                            .build();
                }
            }

            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            double total = 0.0;
            List<OrderDetail> details = new ArrayList<>();

            for (CartDetail cd : cartDetails) {
                ProductVariant pv = cd.getProductVariant();
                int qty = cd.getQuantity();
                double price = pv.getPrice();
                double subtotal = price * qty;

                OrderDetail od = OrderDetail.builder()
                        .order(order)
                        .productVariant(pv)
                        .quantity(qty)
                        .price(price)
                        .subtotal(subtotal)
                        .build();

                details.add(od);
                total += subtotal;

                // Update stock
                pv.setStock(pv.getStock() - qty);
                productVariantRepository.save(pv);
            }

            order.setOrderDetails(details);
            order.setTotalAmount(total);

            Order saved = orderRepository.save(order);

            // Clear cart
            cartDetailRepository.deleteByCart(cart);
            cart.setTotal(0.0);
            cartRepository.save(cart);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("Error in fromCart: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to create order from cart: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== GET ALL ORDERS ===============
    @Override
    public BaseResponse<List<OrderDto>> getAllOrders() {
        try {
            List<OrderDto> orders = orderRepository.findAll()
                    .stream().map(orderMapper::mapToDto)
                    .collect(Collectors.toList());
            return BaseResponse.<List<OrderDto>>builder()
                    .result(Optional.of(orders))
                    .build();
        } catch (Exception e) {
            log.error("Error in getAllOrders: {}", e.getMessage(), e);
            return BaseResponse.<List<OrderDto>>builder()
                    .message(Optional.of("Failed to get orders: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== GET ORDERS BY USER ===============
    @Override
    public BaseResponse<List<OrderDto>> getOrdersByUser(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<OrderDto> orders = orderRepository.findByUser(user)
                    .stream().map(orderMapper::mapToDto)
                    .collect(Collectors.toList());

            return BaseResponse.<List<OrderDto>>builder()
                    .result(Optional.of(orders))
                    .build();

        } catch (Exception e) {
            log.error("Error in getOrdersByUser: {}", e.getMessage(), e);
            return BaseResponse.<List<OrderDto>>builder()
                    .message(Optional.of("Failed to get user orders: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== GET ORDER BY ID ===============
    @Override
    public BaseResponse<OrderDto> getOrderById(String orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(order)))
                    .build();

        } catch (Exception e) {
            log.error("Error in getOrderById: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to get order: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== UPDATE STATUS ===============
    @Override
    public BaseResponse<OrderDto> updateStatus(String orderId, UpdateStatusRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            OrderStatus current = order.getStatus();
            OrderStatus next = request.getStatus();

            boolean allowed = switch (current) {
                case PENDING -> next == OrderStatus.CONFIRMED;
                case CONFIRMED -> next == OrderStatus.SHIPPING;
                case SHIPPING -> next == OrderStatus.DELIVERED;
                default -> false;
            };

            if (!allowed) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Transition not allowed"))
                        .result(Optional.empty())
                        .build();
            }

            order.setStatus(next);
            order.setUpdatedAt(LocalDateTime.now());

            Order saved = orderRepository.save(order);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("Error in updateStatus: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to update status: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    // =============== CANCEL ORDER ===============
    @Override
    public BaseResponse<OrderDto> cancelOrder(String orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED)) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Only PENDING or CONFIRMED orders can be cancelled"))
                        .result(Optional.empty())
                        .build();
            }

            if (order.getOrderDetails() != null) {
                for (OrderDetail od : order.getOrderDetails()) {
                    ProductVariant pv = od.getProductVariant();
                    pv.setStock(pv.getStock() + od.getQuantity());
                    productVariantRepository.save(pv);
                }
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());

            Order saved = orderRepository.save(order);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("Error in cancelOrder: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to cancel order: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }
}
