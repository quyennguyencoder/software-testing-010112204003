package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Top Selling Products
 * Used for dashboard top products list/chart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    
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
     * Total quantity sold
     */
    private Long totalSold;
    
    /**
     * Total revenue generated from this product
     */
    private BigDecimal revenue;
}
