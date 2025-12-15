package com.ds.project.common.mapper;

import com.ds.project.app_context.models.Category;
import com.ds.project.common.entities.dto.request.CategoryRequest;
import com.ds.project.common.entities.dto.response.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for Category Entity ↔ DTO
 */
@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        if (request == null) return null;
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .build();
    }

    public void updateEntity(Category category, CategoryRequest request) {
        if (category == null || request == null) return;

        if (request.getName() != null)
            category.setName(request.getName());

        if (request.getDescription() != null)
            category.setDescription(request.getDescription());

        if (request.getUrl() != null)
            category.setUrl(request.getUrl());
    }

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .slug(category.getSlug())
                .url(category.getUrl())
                .build();
    }
}
