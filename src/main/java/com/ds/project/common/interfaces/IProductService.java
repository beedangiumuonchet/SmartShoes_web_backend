package com.ds.project.common.interfaces;

import com.ds.project.common.entities.common.PaginationResponse;
import com.ds.project.common.entities.dto.request.ProductFilterRequest;
import com.ds.project.common.entities.dto.request.ProductRequest;
import com.ds.project.common.entities.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interface for Product Service
 */
public interface IProductService {

    /**
     * Create a new product
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Get all products
     */
    PaginationResponse<ProductResponse> getAllProducts(ProductFilterRequest filter);
    /**
     * Get product by ID
     */
    ProductResponse getProductById(String id);

    ProductResponse getProductBySlug(String slug);

    PaginationResponse<ProductResponse> getProductsByBrand(String brandId, ProductFilterRequest filter);
    PaginationResponse<ProductResponse> getProductsByCategory(String categoryId, ProductFilterRequest filter);
}
