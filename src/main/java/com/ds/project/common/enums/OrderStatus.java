package com.ds.project.common.enums;

public enum OrderStatus {
    PENDING,        // Đơn hàng vừa được tạo, chờ xác nhận
    PAID,           // Thanh toán thành công,
    CONFIRMED,      // Đơn hàng đã được xác nhận (chuẩn bị giao)
    SHIPPING,       // Đơn hàng đang được giao
    DELIVERED,      // Đơn hàng đã giao thành công
    CANCELLED       // Đơn hàng bị hủy
}
