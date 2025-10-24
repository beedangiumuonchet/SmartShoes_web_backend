package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.ProductVariantRequest;
import com.ds.project.common.entities.dto.response.ProductVariantResponse;

import java.util.List;

public interface IProductVariantService {

    ProductVariantResponse createVariant(String productId, ProductVariantRequest request);

    List<ProductVariantResponse> getVariantsByProductId(String productId);

    ProductVariantResponse updateVariant(String id, ProductVariantRequest request);

    ProductVariantResponse getVariantById(String id);

    void deleteVariant(String id);
}
