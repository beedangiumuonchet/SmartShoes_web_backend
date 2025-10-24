package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.dto.request.ProductImageRequest;
import com.ds.project.common.entities.dto.response.ProductImageResponse;
import com.ds.project.common.interfaces.IProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Product Images
 */
@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final IProductImageService productImageService;

    /**
     * Create new product image for a variant
     */
    @PostMapping("/{productVariantId}")
    public ResponseEntity<ProductImageResponse> createProductImage(
            @PathVariable String productVariantId,
            @Valid @RequestBody ProductImageRequest request) {
        ProductImageResponse response = productImageService.create(request, productVariantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a product image by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductImageResponse> updateProductImage(
            @PathVariable String id,
            @Valid @RequestBody ProductImageRequest request) {
        ProductImageResponse response = productImageService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a product image by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductImageResponse> getProductImageById(@PathVariable String id) {
        ProductImageResponse response = productImageService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all images by ProductVariant ID
     */
    @GetMapping("/variant/{productVariantId}")
    public ResponseEntity<List<ProductImageResponse>> getImagesByVariantId(@PathVariable String productVariantId) {
        List<ProductImageResponse> responses = productImageService.getAllByVariantId(productVariantId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Delete a product image by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable String id) {
        productImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
