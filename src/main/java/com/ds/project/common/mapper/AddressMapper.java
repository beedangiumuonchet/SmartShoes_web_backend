package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Address;
import com.ds.project.common.entities.dto.AddressDto;
import com.ds.project.common.entities.dto.request.AddressRequest;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDto mapToDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .ward(address.getWard())
                .city(address.getCity())
                .isDefault(address.getIsDefault())
                .build();
    }

    public Address mapToEntity(AddressRequest request) {
        return Address.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .ward(request.getWard())
                .city(request.getCity())
                .isDefault(request.getIsDefault())
                .build();
    }
}
