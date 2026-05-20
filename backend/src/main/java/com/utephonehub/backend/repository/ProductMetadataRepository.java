package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.ProductMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ProductMetadata entity
 * Handles queries for product technical specifications
 */
@Repository
public interface ProductMetadataRepository extends JpaRepository<ProductMetadata, Long> {

    /**
     * Find metadata by product ID
     */
    Optional<ProductMetadata> findByProductId(Long productId);

    /**
     * Check if metadata exists for a product
     */
    boolean existsByProductId(Long productId);

    /**
     * Delete metadata by product ID
     */
    void deleteByProductId(Long productId);
}
