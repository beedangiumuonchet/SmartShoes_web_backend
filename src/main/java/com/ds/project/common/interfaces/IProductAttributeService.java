package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.ProductAttributeRequest;
import com.ds.project.common.entities.dto.response.ProductAttributeResponse;

import java.util.List;

/**
 * Interface for ProductAttribute Service
 */
public interface IProductAttributeService {

    /**
     * Add an attribute to a product
     */
    ProductAttributeResponse addProductAttribute(String productId, ProductAttributeRequest request);

    /**
     * Get all attributes of a product
     */
    List<ProductAttributeResponse> getProductAttributes(String productId);

    /**
     * Delete a product attribute by ID
     */
    void deleteProductAttribute(String id);
}
