package com.utephonehub.backend.dto.response.product;

import com.utephonehub.backend.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ProductTemplate response
 * Used in ProductDetailResponse to show all variants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTemplateResponse {

    private Long id;
    
    /**
     * SKU - Unique identifier for this variant
     */
    private String sku;
    
    /**
     * Variant attributes
     */
    private String color;
    private String storage;
    private String ram;
    
    /**
     * Price and stock for this variant
     */
    private BigDecimal price;
    private Integer stockQuantity;
    private StockStatus stockStatus;
    
    /**
     * Template status
     */
    private Boolean status;
    
    /**
     * Audit info
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
