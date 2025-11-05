package com.ds.project.application.controllers.v1;

import com.ds.project.business.v1.services.ProductVariantService;
import com.ds.project.common.entities.dto.request.ProductVariantRequest;
import com.ds.project.common.entities.dto.response.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for ProductVariant
 */
@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private static final Logger log = LoggerFactory.getLogger(ProductVariantController.class);
    private final ProductVariantService productVariantService;

    /**
     * Create a new product variant for a given product
     */
    @PostMapping("/{productId}")
    public ResponseEntity<?> createVariant(
            @PathVariable String productId,
            @RequestBody ProductVariantRequest request) {
        try {
            ProductVariantResponse response = productVariantService.createVariant(productId, request);
            log.info("✅ Created variant successfully for productId {} (color={}, size={})",
                    productId, request.getColorId(), request.getSize());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to create variant for productId {}: {}", productId, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to create variant: " + e.getMessage());
        }
    }

    /**
     * Get all variants by product ID
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getVariantsByProductId(@PathVariable String productId) {
        try {
            List<ProductVariantResponse> variants = productVariantService.getVariantsByProductId(productId);
            log.info("📦 Fetched {} variants for productId {}", variants.size(), productId);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            log.error("❌ Failed to fetch variants for productId {}: {}", productId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to fetch variants: " + e.getMessage());
        }
    }

    /**
     * Get variant detail by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariantById(@PathVariable String id) {
        try {
            ProductVariantResponse variant = productVariantService.getVariantById(id);
            log.info("🔍 Fetched variant detail for ID: {}", id);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            log.error("❌ Failed to fetch variant {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Variant not found or error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/with-product")
    public ResponseEntity<?> getVariantWithProduct(@PathVariable String id) {
        try {
            var variant = productVariantService.getVariantWithProductById(id);
            log.info("🔍 Fetched variant with product for ID: {}", id);
            return ResponseEntity.ok(variant);
        } catch (Exception e) {
            log.error("❌ Failed to fetch variant with product {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to fetch variant with product: " + e.getMessage());
        }
    }


    /**
     * Update variant by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariant(
            @PathVariable String id,
            @RequestBody ProductVariantRequest request) {
        try {
            ProductVariantResponse updated = productVariantService.updateVariant(id, request);
            log.info("📝 Updated variant successfully: id={}, color={}, size={}",
                    id, request.getColorId(), request.getSize());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("❌ Failed to update variant {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to update variant: " + e.getMessage());
        }
    }

    /**
     * Delete variant by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariant(@PathVariable String id) {
        try {
            productVariantService.deleteVariant(id);
            log.info("🗑️ Deleted variant successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Failed to delete variant {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to delete variant: " + e.getMessage());
        }
    }
}
