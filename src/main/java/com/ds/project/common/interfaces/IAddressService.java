package com.ds.project.common.interfaces;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.AddressDto;
import com.ds.project.common.entities.dto.request.AddressRequest;

import java.util.List;

public interface IAddressService {
    BaseResponse<List<AddressDto>> getUserAddresses(String userId);

    BaseResponse<AddressDto> createAddress(String userId, AddressRequest request);

    BaseResponse<AddressDto> updateAddress(String addressId, AddressRequest request);

    void deleteAddress(String addressId);

    BaseResponse<AddressDto> setDefaultAddress(String addressId);
}
