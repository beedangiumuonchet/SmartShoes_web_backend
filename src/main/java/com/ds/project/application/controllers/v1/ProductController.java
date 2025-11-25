package com.ds.project.application.controllers.v1;

import com.ds.project.business.v1.services.ProductService;
import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.AiSearchRequest;
import com.ds.project.common.entities.dto.request.ProductFilterRequest;
import com.ds.project.common.entities.dto.request.ProductRequest;
import com.ds.project.common.entities.dto.response.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ds.project.common.entities.dto.response.ProductVariantWithProductResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@ModelAttribute ProductRequest request) {
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
     * Update an existing Product by ID
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @ModelAttribute ProductRequest request
    ) {
        try {
            log.info("🔄 Updating product ID: {}", id);
            ProductResponse updatedProduct = productService.updateProduct(id, request);
            log.info("✅ Updated product successfully: {}", updatedProduct.getName());
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            log.error("❌ Failed to update product {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update product: " + e.getMessage());
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


//    @PostMapping("/search-image")
//    @Transactional
//    public ResponseEntity<List<ProductImageResponse>> searchSimilarImages(
//            @RequestParam("file") MultipartFile file) {
//
//        if (file == null || file.isEmpty()) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        JsonNode responseJson = cbirService.searchImage(file); // gọi Flask API, trả về JsonNode
//        List<ProductImageResponse> results = new ArrayList<>();
//
//        // Giả sử Flask trả {"results": [ { "id": "...", "imagePath": "...", "documentId": "..." } ]}
//        if (responseJson.has("results")) {
//            for (JsonNode item : responseJson.get("results")) {
//                String imageId = item.has("id") && !item.get("id").isNull()
//                        ? item.get("id").asText()
//                        : null;
//
//                // Tìm trong DB nếu có
//                ProductImage productImage = null;
//                if (imageId != null) {
//                    productImage = productImageRepository.findById(imageId).orElse(null);
//                }
//
//                results.add(ProductImageResponse.builder()
//                        .id(productImage != null ? productImage.getId() : imageId)
//                        .url(productImage != null ? productImage.getUrl() : item.get("imagePath").asText())
//                        .productVariantId(productImage != null && productImage.getProductVariant() != null
//                                ? productImage.getProductVariant().getId()
//                                : null)
////                        .embedding(productImage != null ? productImage.getEmbedding() : null)
//                        .build());
//            }
//        }
//
//        return ResponseEntity.ok(results);
//    }
@PostMapping("/search-image")
@Transactional
public ResponseEntity<List<ProductResponse>> searchSimilarImages(
        @RequestParam("file") MultipartFile file) {

    if (file == null || file.isEmpty()) {
        return ResponseEntity.badRequest().build();
    }

    try {
        // 🔹 Gọi service mới, service sẽ trả về danh sách Product duy nhất
        List<ProductResponse> results = productService.searchSimilarImages(file);

        return ResponseEntity.ok(results);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}



    @PostMapping("/ai/search")
    public ResponseEntity<?> searchAi(@RequestBody AiSearchRequest req) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonReq = mapper.writeValueAsString(req);
            log.info("Sending AI search request JSON: {}", jsonReq);

            AiSearchResponse response = aiService.searchAi(req);

            String jsonResp = mapper.writeValueAsString(response);
            log.info("Received AI search response JSON: {}", jsonResp);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ AI search failed", e);
            return ResponseEntity.internalServerError()
                    .body("AI search error: " + e.getMessage());
        }
    }



}

