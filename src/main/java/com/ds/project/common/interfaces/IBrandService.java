package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.BrandRequest;
import com.ds.project.common.entities.dto.response.BrandResponse;

import java.util.List;

/**
 * Interface for Brand Service
 */
public interface IBrandService {
    BrandResponse createBrand(BrandRequest request);
    BrandResponse updateBrand(String id, BrandRequest request);
    BrandResponse getBrandById(String id);
    BrandResponse getBrandBySlug(String slug);
    List<BrandResponse> getAllBrands();
    void deleteBrand(String id);
}
