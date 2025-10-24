package com.ds.project.application.controllers.v1;

import com.ds.project.business.v1.services.ProductAttributeService;
import com.ds.project.common.entities.dto.request.ProductAttributeRequest;
import com.ds.project.common.entities.dto.response.ProductAttributeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/product-attributes")
@RequiredArgsConstructor
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;

    /**
     * Gắn attribute có sẵn vào product
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ProductAttributeResponse> addAttribute(
            @PathVariable String productId,
            @Validated @RequestBody ProductAttributeRequest request
    ) {
        log.info("Adding attribute {} to product {}", request.getAttributeId(), productId);
        ProductAttributeResponse response = productAttributeService.addProductAttribute(productId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách attribute của 1 product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductAttributeResponse>> getAttributes(@PathVariable String productId) {
        log.info("Fetching attributes for product {}", productId);
        return ResponseEntity.ok(productAttributeService.getProductAttributes(productId));
    }

    /**
     * Xoá 1 attribute gắn với product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable String id) {
        log.info("Deleting product attribute id={}", id);
        productAttributeService.deleteProductAttribute(id);
        return ResponseEntity.noContent().build();
    }
}
