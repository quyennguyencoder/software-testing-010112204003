package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho so sánh sản phẩm
 * Cho phép so sánh tối đa 4 sản phẩm cùng lúc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductComparisonResponse {
    
    private List<ComparisonProduct> products;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonProduct {
        
        // Basic Info
        private Long id;
        private String name;
        private String thumbnailUrl;
        private String brandName;
        
        // Price (single template = single price)
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
        private Boolean hasDiscount;
        
        // Rating
        private Double averageRating;
        private Integer totalReviews;
        
        // Stock
        private Boolean inStock;
        
        // Technical Specs for Comparison
        private ComparisonSpecs specs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonSpecs {
        private String screen;
        private String os;
        private String frontCamera;
        private String rearCamera;
        private String cpu;
        private String ram;
        private String internalMemory;
        private String battery;
        private String charging;
        private String weight;
        private String dimensions;
        private String connectivity;
        private String sim;
        private String materials;
    }
}
