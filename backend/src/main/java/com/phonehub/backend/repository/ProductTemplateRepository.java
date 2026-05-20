package com.phonehub.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.enums.StockStatus;

/**
 * Repository for ProductTemplate entity
 * Handles queries for product variants (SKU-based)
 */
@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long> {

    // find by price greater than
    List<ProductTemplate> findByPriceGreaterThan(BigDecimal price);

    /**
     * Find template by SKU (unique identifier)
     */
    Optional<ProductTemplate> findBySku(String sku);

    /**
     * Check if SKU already exists
     */
    boolean existsBySku(String sku);

    /**
     * Find all templates for a product
     */
    List<ProductTemplate> findByProductId(Long productId);

    /**
     * Find active templates for a product
     */
    @Query("SELECT pt FROM ProductTemplate pt WHERE pt.product.id = :productId AND pt.status = true")
    List<ProductTemplate> findActiveTemplatesByProductId(@Param("productId") Long productId);

    /**
     * Find templates by stock status
     */
    Page<ProductTemplate> findByStockStatus(StockStatus stockStatus, Pageable pageable);

    /**
     * Find low stock templates (stock <= threshold)
     */
    @Query("SELECT pt FROM ProductTemplate pt WHERE pt.stockQuantity <= :threshold AND pt.status = true")
    Page<ProductTemplate> findLowStockTemplates(@Param("threshold") Integer threshold, Pageable pageable);

    /**
     * Find templates by price range
     */
    @Query("SELECT pt FROM ProductTemplate pt WHERE pt.price >= :minPrice AND pt.price <= :maxPrice")
    Page<ProductTemplate> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Get total stock quantity for a product (sum of all templates)
     */
    @Query("SELECT COALESCE(SUM(pt.stockQuantity), 0) FROM ProductTemplate pt WHERE pt.product.id = :productId AND pt.status = true")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    /**
     * Get cheapest template for a product
     * Using Spring Data naming convention instead of LIMIT for portability
     */
    Optional<ProductTemplate> findFirstByProductIdAndStatusTrueOrderByPriceAsc(Long productId);
}
