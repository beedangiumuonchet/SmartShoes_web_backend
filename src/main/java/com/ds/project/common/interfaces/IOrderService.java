package com.ds.project.common.interfaces;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.OrderDto;
import com.ds.project.common.entities.dto.request.*;

import java.util.List;

public interface IOrderService {
    BaseResponse<OrderDto> buyNow(String userId, BuyNowRequest request);
    BaseResponse<OrderDto> fromCart(String userId, FromCartRequest request);
    PaginationResponse<OrderDto> getAllOrders(OrderFilterRequest filter);
    BaseResponse<List<OrderDto>> getOrdersByUser(String userId);
    BaseResponse<OrderDto> getOrderById(String orderId);
    BaseResponse<OrderDto> updateStatus(String orderId, UpdateStatusRequest request);
    BaseResponse<OrderDto> updateShipping(String orderId, UpdateShippingRequest request);
    BaseResponse<OrderDto> cancelOrder(String orderId);
}
