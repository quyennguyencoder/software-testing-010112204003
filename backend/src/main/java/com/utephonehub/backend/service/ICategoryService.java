package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.category.CreateCategoryRequest;
import com.utephonehub.backend.dto.request.category.UpdateCategoryRequest;
import com.utephonehub.backend.dto.response.category.CategoryResponse;

import java.util.List;

/**
 * Interface for Category Service operations
 */
public interface ICategoryService {

    /**
     * Get categories by parent ID
     * If parentId is null, return root categories (categories without parent)
     * If parentId is provided, return children of that parent
     *
     * @param parentId Parent category ID (nullable)
     * @return List of CategoryResponse
     */
    List<CategoryResponse> getCategoriesByParentId(Long parentId);

    /**
     * Create new category
     * @param request CreateCategoryRequest
     * @return CategoryResponse
     */
    CategoryResponse createCategory(CreateCategoryRequest request);

    /**
     * Update existing category
     * @param id Category ID
     * @param request UpdateCategoryRequest
     * @return CategoryResponse
     */
    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    /**
     * Delete category by ID
     * Check constraints before deletion:
     * - Cannot delete if category has children
     * - Cannot delete if category has products linked
     * @param id Category ID
     */
    void deleteCategory(Long id);
}

