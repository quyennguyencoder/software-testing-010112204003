package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Low Stock Products
 * Used for dashboard low stock warning list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductResponse {
    
    /**
     * Product ID
     */
    private Long productId;
    
    /**
     * Product name
     */
    private String productName;
    
    /**
     * Product image URL
     */
    private String imageUrl;
    
    /**
     * Current stock quantity
     */
    private Integer stockQuantity;
    
    /**
     * Category name
     */
    private String categoryName;
    
    /**
     * Brand name
     */
    private String brandName;
    
    /**
     * Product status (active/inactive)
     */
    private Boolean status;
}
