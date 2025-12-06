package com.ds.project.common.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopCustomerDTO {

    private UserDto customer;   // Thông tin khách hàng
    private int totalOrders;             // Tổng số đơn hàng thành công
    private double totalSpent;           // Tổng tiền đã mua
}
