package com.ds.project.application.controllers.v1;

import com.ds.project.business.v1.services.ProductService;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.ProductFilterRequest;
import com.ds.project.common.entities.dto.request.ProductRequest;
import com.ds.project.common.entities.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Product
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    /**
     * Create a new Product
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.createProduct(request);
            log.info("✅ Created product successfully: {}", response.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to create product: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to create product: " + e.getMessage());
        }
    }

    /**
     * Get all products
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(@ModelAttribute ProductFilterRequest filter) {
        try {
            PaginationResponse<ProductResponse> response = productService.getAllProducts(filter);
            log.info("📦 Fetched {} products (page {})", response.getContent().size(), response.getPage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch products: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get products: " + e.getMessage());
        }
    }

    /**
     * Get product detail by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            ProductResponse product = productService.getProductById(id);
            log.info("🔍 Fetched product detail for ID: {}", id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("❌ Failed to get product {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Product not found or error occurred: " + e.getMessage());
        }
    }

    /**
     * ✅ Get product detail by Slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        try {
            ProductResponse product = productService.getProductBySlug(slug);
            log.info("🔍 Fetched product detail for slug: {}", slug);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("❌ Failed to get product by slug {}: {}", slug, e.getMessage());
            return ResponseEntity.badRequest().body("Product not found or error occurred: " + e.getMessage());
        }
    }

    /**
     * Get products by Brand ID with optional filters and pagination
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<?> getProductsByBrand(
            @PathVariable String brandId,
            @ModelAttribute ProductFilterRequest filter
    ) {
        try {
            PaginationResponse<ProductResponse> response = productService.getProductsByBrand(brandId, filter);
            log.info("📦 Fetched {} products for Brand ID: {} (page {})", response.getContent().size(), brandId, response.getPage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch products by Brand {}: {}", brandId, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get products by brand: " + e.getMessage());
        }
    }

    /**
     * Get products by Category ID with optional filters and pagination
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable String categoryId,
            @ModelAttribute ProductFilterRequest filter
    ) {
        try {
            PaginationResponse<ProductResponse> response = productService.getProductsByCategory(categoryId, filter);
            log.info("📦 Fetched {} products for Category ID: {} (page {})", response.getContent().size(), categoryId, response.getPage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Failed to fetch products by Category {}: {}", categoryId, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to get products by category: " + e.getMessage());
        }
    }

}

