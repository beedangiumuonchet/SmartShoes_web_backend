package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.ProductImageRequest;
import com.ds.project.common.entities.dto.response.ProductImageResponse;
import java.util.List;

public interface IProductImageService {

    ProductImageResponse create(ProductImageRequest request, String productVariantId);

    ProductImageResponse update(String id, ProductImageRequest request);

    void delete(String id);

    List<ProductImageResponse> getAllByVariantId(String productVariantId);

    ProductImageResponse getById(String id);
}
