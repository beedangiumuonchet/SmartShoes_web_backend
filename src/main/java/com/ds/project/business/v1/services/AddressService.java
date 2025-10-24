package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Address;
import com.ds.project.app_context.models.User;
import com.ds.project.app_context.repositories.AddressRepository;
import com.ds.project.app_context.repositories.UserRepository;
import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.AddressDto;
import com.ds.project.common.entities.dto.request.AddressRequest;
import com.ds.project.common.interfaces.IAddressService;
import com.ds.project.common.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public BaseResponse<List<AddressDto>> getUserAddresses(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<AddressDto> addresses = addressRepository.findByUser(user).stream()
                    .map(addressMapper::mapToDto)
                    .collect(Collectors.toList());

            return BaseResponse.<List<AddressDto>>builder()
                    .result(Optional.of(addresses))
                    .build();
        } catch (Exception e) {
            log.error("Error getting addresses for user {}: {}", userId, e.getMessage(), e);
            return BaseResponse.<List<AddressDto>>builder()
                    .message(Optional.of("Failed to get addresses: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public BaseResponse<AddressDto> createAddress(String userId, AddressRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Address address = addressMapper.mapToEntity(request);
            address.setUser(user);

            if (Boolean.TRUE.equals(request.getIsDefault())) {
                addressRepository.clearDefaultAddress(userId);
                address.setIsDefault(true);
            }

            Address saved = addressRepository.save(address);
            return BaseResponse.<AddressDto>builder()
                    .result(Optional.of(addressMapper.mapToDto(saved)))
                    .build();
        } catch (Exception e) {
            log.error("Error creating address for user {}: {}", userId, e.getMessage(), e);
            return BaseResponse.<AddressDto>builder()
                    .message(Optional.of("Failed to create address: " + e.getMessage()))
                    .result(Optional.empty())
                    .build();
        }
    }

    @Override
    public BaseResponse<AddressDto> updateAddress(String addressId, AddressRequest request) {
        try {
            Address existing = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            existing.setFullName(request.getFullName());
            existing.setPhone(request.getPhone());
            existing.setStreet(request.getStreet());
            existing.setWard(request.getWard());
            existing.setCity(request.getCity());
            existing.setUpdatedAt(LocalDateTime.now());

            if (Boolean.TRUE.equals(request.getIsDefault())) {
                addressRepository.clearDefaultAddress(existing.getUser().getId());
                existing.setIsDefault(true);
            }

            Address updated = addressRepository.save(existing);
            return BaseResponse.<AddressDto>builder()
                    .result(Optional.of(addressMapper.mapToDto(updated)))
                    .build();
        } catch (Exception e) {
            log.error("Error updating address {}: {}", addressId, e.getMessage(), e);
            return BaseResponse.<AddressDto>builder()
                    .message(Optional.of("Failed to update address: " + e.getMessage()))
                    .build();
        }
    }

    @Override
    public void deleteAddress(String addressId) {
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            addressRepository.delete(address);
            log.info("Deleted address {}", addressId);
        } catch (Exception e) {
            log.error("Error deleting address {}: {}", addressId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete address: " + e.getMessage());
        }
    }

    @Override
    public BaseResponse<AddressDto> setDefaultAddress(String addressId) {
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            addressRepository.clearDefaultAddress(address.getUser().getId());
            address.setIsDefault(true);

            Address updated = addressRepository.save(address);
            return BaseResponse.<AddressDto>builder()
                    .result(Optional.of(addressMapper.mapToDto(updated)))
                    .build();
        } catch (Exception e) {
            log.error("Error setting default address {}: {}", addressId, e.getMessage(), e);
            return BaseResponse.<AddressDto>builder()
                    .message(Optional.of("Failed to set default address: " + e.getMessage()))
                    .build();
        }
    }
}
