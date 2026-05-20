package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Get all brands ordered by name
     * @return List of brands
     */
    List<Brand> findAllByOrderByNameAsc();

    /**
     * Check if brand name exists
     * Used for create operations to prevent duplicate names
     * @param name Brand name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Check if brand name exists (excluding specified ID)
     * Used for update operations to prevent duplicate names
     * @param name Brand name
     * @param id Brand ID to exclude from check
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long id);
}

