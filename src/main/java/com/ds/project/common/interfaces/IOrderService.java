package com.ds.project.common.interfaces;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.request.BuyNowRequest;
import com.ds.project.common.entities.dto.request.FromCartRequest;
import com.ds.project.common.entities.dto.request.UpdateStatusRequest;

import java.util.List;

public interface IOrderService {
    BaseResponse<OrderDto> buyNow(String userId, BuyNowRequest request);
    BaseResponse<OrderDto> fromCart(String userId, FromCartRequest request);
    BaseResponse<List<OrderDto>> getAllOrders();
    BaseResponse<List<OrderDto>> getOrdersByUser(String userId);
    BaseResponse<OrderDto> getOrderById(String orderId);
    BaseResponse<OrderDto> updateStatus(String orderId, UpdateStatusRequest request);
    BaseResponse<OrderDto> cancelOrder(String orderId);
}
