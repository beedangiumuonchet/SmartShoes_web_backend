package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.dto.request.PromotionProductRequest;
import com.ds.project.common.entities.dto.response.PromotionProductResponse;
import com.ds.project.common.interfaces.IPromotionProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing PromotionProduct
 */
@RestController
@RequestMapping("/api/promotion-products")
@RequiredArgsConstructor
public class PromotionProductController {

    private final IPromotionProductService promotionProductService;

    @PostMapping
    public ResponseEntity<PromotionProductResponse> create(@Valid @RequestBody PromotionProductRequest request) {
        return ResponseEntity.ok(promotionProductService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionProductResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(promotionProductService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromotionProductResponse>> getAll() {
        return ResponseEntity.ok(promotionProductService.getAll());
    }

    @GetMapping("/promotion/{promotionId}")
    public ResponseEntity<List<PromotionProductResponse>> getByPromotionId(@PathVariable String promotionId) {
        return ResponseEntity.ok(promotionProductService.getByPromotionId(promotionId));
    }

    @GetMapping("/variant/{productVariantId}")
    public ResponseEntity<List<PromotionProductResponse>> getByProductVariantId(@PathVariable String productVariantId) {
        return ResponseEntity.ok(promotionProductService.getByProductVariantId(productVariantId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        promotionProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
