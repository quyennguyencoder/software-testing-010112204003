package com.utephonehub.backend.entity;

import com.utephonehub.backend.enums.StockStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductTemplate Entity - Represents product variants (SKU-based)
 * Aligned with Class Diagram: Product 1-N ProductTemplate
 * 
 * Each template represents a specific variant of a product:
 * - iPhone 15 Pro Max 256GB Titan Black (SKU: IP15PM-256-BLK)
 * - iPhone 15 Pro Max 512GB Titan White (SKU: IP15PM-512-WHT)
 */
@Entity
@Table(name = "product_templates", indexes = {
    @Index(name = "idx_product_template_sku", columnList = "sku"),
    @Index(name = "idx_product_template_product_id", columnList = "product_id"),
    @Index(name = "idx_product_template_stock_status", columnList = "stock_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to parent Product entity
     * Many templates belong to one product
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Stock Keeping Unit - Unique identifier for this variant
     * Format: PRODUCT_CODE-STORAGE-COLOR (e.g., IP15PM-256-BLK)
     */
    @Column(unique = true, nullable = false, length = 100)
    private String sku;

    /**
     * Variant attributes
     */
    @Column(length = 50)
    private String color; // Titan Black, Natural Titanium, etc.

    @Column(length = 50)
    private String storage; // 256GB, 512GB, 1TB

    @Column(length = 50)
    private String ram; // 8GB, 12GB, 16GB (for laptops)

    /**
     * Pricing for this specific variant
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Stock quantity for this variant
     * Updated when orders are placed/cancelled
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    /**
     * Stock status automatically derived from stockQuantity
     * IN_STOCK: quantity > 10
     * LOW_STOCK: 1 <= quantity <= 10
     * OUT_OF_STOCK: quantity = 0
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    /**
     * Template active status
     * Inactive templates won't be shown to customers
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;

    // Audit fields
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Update stock status based on quantity
     * Called before persist/update
     */
    @PrePersist
    @PreUpdate
    public void updateStockStatus() {
        if (stockQuantity == null || stockQuantity == 0) {
            this.stockStatus = StockStatus.OUT_OF_STOCK;
        } else if (stockQuantity <= 10) {
            this.stockStatus = StockStatus.LOW_STOCK;
        } else {
            this.stockStatus = StockStatus.IN_STOCK;
        }
    }
}
