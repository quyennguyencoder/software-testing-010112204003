package com.utephonehub.backend.dto.response.product;

import com.utephonehub.backend.dto.response.category.CategoryResponse;
import com.utephonehub.backend.dto.response.brand.BrandResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for detailed Product response
 * Aligned with Class Diagram: includes templates (variants) and metadata (specs)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    /**
     * Core product information
     */
    private Long id;
    
    private String name;
    
    private String description;
    
    private String thumbnailUrl;
    
    private Boolean status;
    
    private CategoryResponse category;
    
    private BrandResponse brand;
    
    /**
     * Product variants (templates)
     * Each template has: SKU, color, storage, RAM, price, stockQuantity, stockStatus
     */
    @Builder.Default
    private List<ProductTemplateResponse> templates = new ArrayList<>();
    
    /**
     * Technical specifications (metadata)
     * Contains: screen, CPU, camera, battery, etc.
     */
    private ProductMetadataResponse metadata;
    
    /**
     * Product images
     */
    @Builder.Default
    private List<ProductImageResponse> images = new ArrayList<>();
    
    /**
     * Audit information
     */
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdByUsername;
    
    private String updatedByUsername;
}
