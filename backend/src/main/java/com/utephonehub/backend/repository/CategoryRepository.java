package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdIsNull();
    List<Category> findByParentId(Long parentId);

    /**
     * Check if category name exists in same parent level
     * @param name Category name
     * @param parentId Parent category ID (null for root level)
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndParentId(String name, Long parentId);

    /**
     * Check if category name exists in same parent level (excluding specified ID)
     * Used for update operations to prevent duplicate names
     * @param name Category name
     * @param parentId Parent category ID (null for root level)
     * @param id Category ID to exclude from check
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndParentIdAndIdNot(String name, Long parentId, Long id);

    /**
     * Count categories by parent ID
     * Used to check if a category has children before deletion
     * @param parentId Parent category ID
     * @return Number of child categories
     */
    long countByParentId(Long parentId);
}

