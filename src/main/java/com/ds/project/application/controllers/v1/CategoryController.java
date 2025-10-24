package com.ds.project.application.controllers.v1;

import com.ds.project.common.entities.dto.request.CategoryRequest;
import com.ds.project.common.entities.dto.response.CategoryResponse;
import com.ds.project.common.interfaces.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Categories
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * Create a new category
     */
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {
        try {
            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create category: " + e.getMessage());
        }
    }

    /**
     * Get all categories
     */
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryResponse> responses = categoryService.getAllCategories();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to fetch categories: " + e.getMessage());
        }
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            CategoryResponse response = categoryService.getCategoryById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching category by id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to fetch category: " + e.getMessage());
        }
    }

    /**
     * Update category by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @RequestBody CategoryRequest request) {
        try {
            CategoryResponse response = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update category: " + e.getMessage());
        }
    }

    /**
     * Delete category by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to delete category: " + e.getMessage());
        }
    }
}

