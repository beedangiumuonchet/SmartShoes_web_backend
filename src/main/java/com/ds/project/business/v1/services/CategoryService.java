package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Category;
import com.ds.project.app_context.repositories.CategoryRepository;
import com.ds.project.common.entities.dto.request.CategoryRequest;
import com.ds.project.common.entities.dto.response.CategoryResponse;
import com.ds.project.common.interfaces.ICategoryService;
import com.ds.project.common.mapper.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = mapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id=" + id));

        mapper.updateEntity(category, request);
        Category updated = categoryRepository.save(category);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id=" + id));
        return mapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found id=" + id);
        }
        categoryRepository.deleteById(id);
    }
}
