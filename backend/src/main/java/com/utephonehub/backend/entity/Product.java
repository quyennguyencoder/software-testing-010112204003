package com.utephonehub.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product Entity - Core product information
 * Aligned with Class Diagram: Product has many ProductTemplates and one ProductMetadata
 * 
 * Represents the base product without variants:
 * - Product: iPhone 15 Pro Max
 *   - Templates: 256GB Black, 512GB White, etc. (price/stock per variant)
 *   - Metadata: Technical specs (screen, camera, battery)
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Product name (e.g., "iPhone 15 Pro Max", "MacBook Air M3")
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Detailed product description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Main product thumbnail image URL
     */
    @Column(length = 255)
    private String thumbnailUrl;

    /**
     * Product active status (shown to customers if true)
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;

    /**
     * Relationships
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /**
     * Product Variants (Templates)
     * One product has many templates (color/storage/RAM variants)
     * Cascade: Save templates when saving product
     * Orphan removal: Delete templates if removed from list
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductTemplate> templates = new ArrayList<>();

    /**
     * Technical Specifications (Metadata)
     * One product has one metadata record
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProductMetadata metadata;

    /**
     * Product Images
     * Multiple images per product for gallery
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Soft Delete fields
    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    // Audit Tracking fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Returns mutable list for JPA, MapStruct, and internal operations
     */
    public List<ProductTemplate> getTemplates() {
        return templates;
    }

    /**
     * Public method to get immutable view of templates for safe external access
     */
    public List<ProductTemplate> getTemplatesView() {
        return Collections.unmodifiableList(templates);
    }

    /**
     * Helper method to clear all templates
     * Properly handles orphan removal
     */
    public void clearTemplates() {
        templates.clear();
    }

    /**
     * Helper method to add template to product
     * Maintains bidirectional relationship
     */
    public void addTemplate(ProductTemplate template) {
        templates.add(template);
        template.setProduct(this);
    }

    /**
     * Helper method to set metadata
     * Maintains bidirectional relationship
     * Clears old relationship if metadata is being replaced
     */
    public void setMetadata(ProductMetadata metadata) {
        // Clear old relationship if exists
        if (this.metadata != null && this.metadata != metadata) {
            this.metadata.setProduct(null);
        }
        
        this.metadata = metadata;
        
        // Set bidirectional relationship
        if (metadata != null && metadata.getProduct() != this) {
            metadata.setProduct(this);
        }
    }
}

