package com.ds.project.common.interfaces;

import com.ds.project.common.entities.dto.request.CategoryRequest;
import com.ds.project.common.entities.dto.response.CategoryResponse;

import java.util.List;

/**
 * Interface for Category Service
 */
public interface ICategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(String id, CategoryRequest request);
    CategoryResponse getCategoryById(String id);
    List<CategoryResponse> getAllCategories();
    void deleteCategory(String id);
}
