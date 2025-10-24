package com.ds.project.common.interfaces;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.CartDto;
import com.ds.project.common.entities.dto.CartDetailDto;
import com.ds.project.common.entities.dto.request.CartDetailRequest;

import java.util.List;

public interface ICartService {
    BaseResponse<CartDto> getUserCart(String userId);
    BaseResponse<CartDto> createCart(String userId);
    BaseResponse<CartDetailDto> addCartDetail(String cartId, CartDetailRequest request);
    BaseResponse<CartDetailDto> updateCartDetail(String detailId, CartDetailRequest request);
    void deleteCartDetail(String detailId);
    void clearCart(String cartId);
}
