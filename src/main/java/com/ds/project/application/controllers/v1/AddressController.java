package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.base.BaseResponse;
import com.ds.project.common.entities.dto.AddressDto;
import com.ds.project.common.entities.dto.request.AddressRequest;
import com.ds.project.common.interfaces.IAddressService;
import com.ds.project.common.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Address operations
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final IAddressService addressService;

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserAddresses(@PathVariable String id) {
        BaseResponse<List<AddressDto>> response = addressService.getUserAddresses(id);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to get addresses"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> createAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {
        BaseResponse<AddressDto> response = addressService.createAddress(id, request);
        if (response.getResult() != null && response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to create address"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressRequest request) {
        BaseResponse<AddressDto> response = addressService.updateAddress(id, request);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to update address"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable String id) {
        try {
            addressService.deleteAddress(id);
            return ResponseUtils.success(null, "Address deleted successfully");
        } catch (Exception e) {
            return ResponseUtils.error("Failed to delete address: " + e.getMessage(),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable String id) {
        BaseResponse<AddressDto> response = addressService.setDefaultAddress(id);
        if (response.getResult().isPresent()) {
            return ResponseUtils.success(response.getResult().get());
        } else {
            return ResponseUtils.error(response.getMessage().orElse("Failed to set default address"),
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}
