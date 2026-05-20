package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.category.CreateCategoryRequest;
import com.utephonehub.backend.dto.request.category.UpdateCategoryRequest;
import com.utephonehub.backend.dto.response.category.CategoryResponse;
import com.utephonehub.backend.entity.Category;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.CategoryRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByParentId(Long parentId) {
        if (parentId == null) {
            // Get root categories (parentId is null)
            log.info("Getting root categories");
            List<Category> rootCategories = categoryRepository.findByParentIdIsNull();
            return rootCategories.stream()
                    .map(this::toCategoryResponseWithCounts)
                    .collect(Collectors.toList());
        } else {
            // Get children of specific parent
            log.info("Getting categories by parent id: {}", parentId);

            // Validate parent exists
            if (!categoryRepository.existsById(parentId)) {
                throw new ResourceNotFoundException("Danh mục cha không tồn tại với ID: " + parentId);
            }

            List<Category> childCategories = categoryRepository.findByParentId(parentId);
            return childCategories.stream()
                    .map(this::toCategoryResponseWithCounts)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Helper method to convert Category entity to CategoryResponse with counts
     */
    private CategoryResponse toCategoryResponseWithCounts(Category category) {
        // Count children
        long childrenCount = categoryRepository.countByParentId(category.getId());

        // Count products (only non-deleted products)
        long productCount = productRepository.countByCategoryIdAndIsDeletedFalse(category.getId());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .hasChildren(childrenCount > 0)
                .childrenCount((int) childrenCount)
                .productCount((int) productCount)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category with name: {} and parentId: {}", request.getName(), request.getParentId());

        // Check if category name already exists in same parent level
        if (categoryRepository.existsByNameAndParentId(request.getName(), request.getParentId())) {
            String parentInfo = request.getParentId() == null
                ? "danh mục gốc"
                : "danh mục cha ID " + request.getParentId();
            throw new BadRequestException("Tên danh mục '" + request.getName() + "' đã tồn tại trong " + parentInfo);
        }

        // If parentId is provided, validate parent exists
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Danh mục cha không tồn tại với ID: " + request.getParentId()));
        }

        // Create new category
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .build();

        category = categoryRepository.save(category);
        log.info("Created category successfully with id: {}", category.getId());
        return toCategoryResponseWithCounts(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category with id: {}, new name: {}, new parentId: {}",
                id, request.getName(), request.getParentId());

        // Find existing category
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại với ID: " + id));

        // Check if new name conflicts with another category in the same parent level
        // (excluding the current category being updated)
        if (categoryRepository.existsByNameAndParentIdAndIdNot(request.getName(), request.getParentId(), id)) {
            String parentInfo = request.getParentId() == null
                    ? "danh mục gốc"
                    : "danh mục cha ID " + request.getParentId();
            throw new BadRequestException("Tên danh mục '" + request.getName() + "' đã tồn tại trong " + parentInfo);
        }

        // If parentId is provided, validate parent exists and prevent circular reference
        Category parent = null;
        if (request.getParentId() != null) {
            // Cannot set parent to itself
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Danh mục không thể là cha của chính nó");
            }

            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Danh mục cha không tồn tại với ID: " + request.getParentId()));
        }

        // Update category fields
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParent(parent);

        category = categoryRepository.save(category);
        log.info("Updated category successfully with id: {}", category.getId());

        return toCategoryResponseWithCounts(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);

        // Check if category exists
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại với ID: " + id));

        // Check if category has children
        long childrenCount = categoryRepository.countByParentId(id);
        if (childrenCount > 0) {
            throw new BadRequestException(
                    "Không thể xóa danh mục. Danh mục này có " + childrenCount + " danh mục con");
        }

        // Check if category has products linked
        boolean hasProducts = productRepository.existsByCategoryId(id);
        if (hasProducts) {
            throw new BadRequestException(
                    "Không thể xóa danh mục. Danh mục đang chứa sản phẩm");
        }

        // Delete category
        categoryRepository.delete(category);
        log.info("Deleted category successfully with id: {}", id);
    }
}

