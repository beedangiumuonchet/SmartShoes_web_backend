package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.*;
import com.ds.project.app_context.repositories.*;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.request.*;
import com.ds.project.common.enums.OrderStatus;
import com.ds.project.common.interfaces.IOrderService;
import com.ds.project.common.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import jakarta.persistence.criteria.Predicate;
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

            Double salePrice = variant.getPriceSale();
            Double regularPrice = variant.getPrice();
            int quantity = request.getQuantity();

            double price; // Giá đơn vị cho OrderDetail

            // Logic ưu tiên giá Sale nếu nó tồn tại (khác null) và thấp hơn giá gốc
            if (salePrice != null && regularPrice != null && salePrice < regularPrice) {
                price = salePrice;
            } else {
                // Nếu giá Sale null/cao hơn/không tồn tại, dùng giá gốc.
                // Xử lý cả trường hợp giá gốc cũng null để tránh NPE
                price = (regularPrice != null) ? regularPrice : 0.0;
            }

            double subtotal = price * quantity; // Tính subtotal bằng giá hiệu lực

            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .totalAmount(subtotal)
                    .shippingName(request.getShippingName())
                    .shippingAddress(request.getShippingAddress())
                    .shippingPhone(request.getShippingPhone())
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
    @Transactional
    public BaseResponse<OrderDto> fromCart(String userId, FromCartRequest request) {
        try {

            // 1. Lấy user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Lấy cart + kiểm tra quyền sở hữu
            Cart cart = cartRepository.findById(request.getCartId())
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            if (!cart.getUser().getId().equals(userId)) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Không thể đặt hàng từ giỏ của người khác"))
                        .result(Optional.empty())
                        .build();
            }

            // 3. Lấy danh sách cart detail
            List<CartDetail> cartDetails = cartDetailRepository.findByCart(cart);

            if (cartDetails == null || cartDetails.isEmpty()) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Giỏ hàng trống"))
                        .result(Optional.empty())
                        .build();
            }

            // ================================
            // 4. Kiểm tra tồn kho TOÀN BỘ (trước khi trừ)
            // ================================
            for (CartDetail cd : cartDetails) {
                int qty = cd.getQuantity();
                if (qty <= 0) {
                    return BaseResponse.<OrderDto>builder()
                            .message(Optional.of("Số lượng sản phẩm không hợp lệ"))
                            .result(Optional.empty())
                            .build();
                }

                ProductVariant pv = cd.getProductVariant();
                if (pv.getStock() == null || pv.getStock() < qty) {
                    return BaseResponse.<OrderDto>builder()
                            .message(Optional.of("Sản phẩm " +
                                    pv.getProduct().getName() + " vượt quá tồn kho"))
                            .result(Optional.empty())
                            .build();
                }
            }

            // ================================
            // 5. Tạo order
            // ================================
            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .shippingName(request.getShippingName())
                    .shippingAddress(request.getShippingAddress())
                    .shippingPhone(request.getShippingPhone())
                    .createdAt(LocalDateTime.now())
                    .build();

            List<OrderDetail> orderDetails = new ArrayList<>();
            double totalAmount = 0.0;

            // ================================
            // 6. Chuyển CartDetail → OrderDetail
            // ================================
            for (CartDetail cd : cartDetails) {

                ProductVariant pv = cd.getProductVariant();
                int qty = cd.getQuantity();
                double price = calculateEffectivePrice(pv); // ⭐ dùng hàm riêng

                double subtotal = qty * price;

                OrderDetail od = OrderDetail.builder()
                        .order(order)
                        .productVariant(pv)
                        .quantity(qty)
                        .price(price)
                        .subtotal(subtotal)
                        .build();

                orderDetails.add(od);
                totalAmount += subtotal;

                // Trừ tồn kho (chỉ trừ sau khi check xong hết)
                pv.setStock(pv.getStock() - qty);
            }

            order.setOrderDetails(orderDetails);
            order.setTotalAmount(totalAmount);

            // Lưu order
            Order savedOrder = orderRepository.save(order);

            // Lưu thay đổi stock
            productVariantRepository.saveAll(
                    cartDetails.stream()
                            .map(CartDetail::getProductVariant)
                            .toList()
            );

            // ================================
            // 7. Clear cart
            // ================================
            cart.getDetails().clear();     // Xoá trong RAM (Hibernate)
            cartDetailRepository.deleteByCart(cart);  // Xoá trong DB
            cartRepository.save(cart);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(savedOrder)))
                    .build();

        } catch (Exception e) {
            log.error("Error in fromCart: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to create order from cart: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }
    private double calculateEffectivePrice(ProductVariant pv) {
        Double salePrice = pv.getPriceSale();
        Double regularPrice = pv.getPrice();

        if (regularPrice == null) return 0.0;
        if (salePrice == null || salePrice >= regularPrice) return regularPrice;

        return salePrice;
    }
    // =============== GET ALL ORDERS ===============
    @Override
    public PaginationResponse<OrderDto> getAllOrders(OrderFilterRequest filter) {
        String sortBy = (filter.getSortBy() != null) ? filter.getSortBy() : "createdAt";
        String sortDir = (filter.getSortDirection() != null) ? filter.getSortDirection() : "desc";
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Order> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Tìm kiếm theo id, tên user hoặc email
            if (filter.getQ() != null && !filter.getQ().isEmpty()) {
                String kw = "%" + filter.getQ().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("id")), kw),
                        cb.like(cb.lower(root.get("user").get("username")), kw),
                        cb.like(cb.lower(root.get("user").get("email")), kw)
                ));
            }

            // Lọc theo userId
            if (filter.getUserId() != null && !filter.getUserId().isEmpty()) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
            }

            // Lọc theo trạng thái đơn hàng
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Lọc theo ngày tạo
            if (filter.getCreatedDate_from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_from().atStartOfDay()));
            }
            if (filter.getCreatedDate_to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedDate_to().atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        var page = orderRepository.findAll(spec, pageable);
        List<OrderDto> content = page.getContent().stream()
                .map(orderMapper::mapToDto)
                .toList();

        log.info("✅ getAllOrders fetched {} records (page {} / {})", content.size(), page.getNumber() + 1, page.getTotalPages());

        return PaginationResponse.<OrderDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
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
    @Transactional
    public BaseResponse<OrderDto> updateStatus(String orderId, UpdateStatusRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            OrderStatus current = order.getStatus();
            OrderStatus next = request.getStatus();

            boolean allowed = switch (current) {
                case PENDING -> next == OrderStatus.PAID || next == OrderStatus.CONFIRMED;
                case PAID -> next == OrderStatus.CONFIRMED;
                case CONFIRMED -> next == OrderStatus.SHIPPING;
                case SHIPPING -> next == OrderStatus.DELIVERED || next == OrderStatus.RETURNED;
                case DELIVERED -> next == OrderStatus.RETURNED;
                default -> false;
            };

            if (!allowed) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Transition not allowed"))
                        .result(Optional.empty())
                        .build();
            }

            // 🔥 HOÀN KHO KHI RETURNED (GIỐNG CANCEL)
            if (next == OrderStatus.RETURNED && current != OrderStatus.RETURNED) {
                if (order.getOrderDetails() != null) {
                    for (OrderDetail od : order.getOrderDetails()) {
                        ProductVariant pv = od.getProductVariant();
                        pv.setStock(pv.getStock() + od.getQuantity());
                        productVariantRepository.save(pv);
                    }
                }
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


    public BaseResponse<OrderDto> updateShipping(String orderId, UpdateShippingRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Chỉ cho phép thay đổi khi PENDING hoặc PAID
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Cannot update shipping info for this order status"))
                        .result(Optional.empty())
                        .build();
            }

            order.setShippingName(request.getShippingName());
            order.setShippingPhone(request.getShippingPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setUpdatedAt(LocalDateTime.now());

            Order saved = orderRepository.save(order);

            return BaseResponse.<OrderDto>builder()
                    .result(Optional.of(orderMapper.mapToDto(saved)))
                    .build();

        } catch (Exception e) {
            log.error("Error in updateShipping: {}", e.getMessage(), e);
            return BaseResponse.<OrderDto>builder()
                    .message(Optional.of("Failed to update shipping info: " + e.getMessage()))
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

            if (!(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CONFIRMED)) {
                return BaseResponse.<OrderDto>builder()
                        .message(Optional.of("Only PENDING, PAID, CONFIRMED orders can be cancelled"))
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
