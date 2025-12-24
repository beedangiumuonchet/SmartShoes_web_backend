package com.ds.project.business.v1.services;

import com.ds.project.app_context.models.Category;
import com.ds.project.app_context.repositories.CategoryRepository;
import com.ds.project.common.entities.dto.request.CategoryRequest;
import com.ds.project.common.entities.dto.response.BrandResponse;
import com.ds.project.common.entities.dto.response.CategoryResponse;
import com.ds.project.common.interfaces.ICategoryService;
import com.ds.project.common.mapper.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        String name = request.getName().trim();

        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = mapper.toEntity(request);

        String slug = toSlug(name);

        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        category.setSlug(slug);

        Category saved = categoryRepository.save(category);
        return mapper.toResponse(saved);
    }


    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Category not found with id=" + id)
                );

        String newName = request.getName().trim();

        boolean isNameChanged =
                newName != null &&
                        !newName.equalsIgnoreCase(category.getName());

        // ❗ CHECK TRÙNG NAME (IGNORE CASE)
        if (isNameChanged &&
                categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {

            throw new IllegalArgumentException("Category name already exists");
        }

        mapper.updateEntity(category, request);

        // 👉 nếu đổi name → sinh lại slug
        if (isNameChanged) {
            String newSlug = toSlug(newName);

            if (categoryRepository.existsBySlugAndIdNot(newSlug, id)) {
                newSlug = newSlug + "-" + System.currentTimeMillis();
            }

            log.info("📦 Updated category slug = {}", newSlug);
            category.setSlug(newSlug);
        }

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
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new EntityNotFoundException("Category not found with slug=" + slug)
                );

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

    public static String toSlug(String input) {
        if (input == null) return null;

        // 1. Chuẩn hóa Unicode (tách dấu)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // 2. Xóa dấu tiếng Việt
        String slug = normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d")
                .replace("Đ", "D");

        // 3. Chuẩn hóa slug
        return slug
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
