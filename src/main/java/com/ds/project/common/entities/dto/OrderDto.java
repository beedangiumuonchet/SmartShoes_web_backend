package com.ds.project.common.entities.dto;

import com.ds.project.common.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String id;
    private OrderStatus status;
    private String userId;
    private Double total_amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetailDto> orderDetails;
}
