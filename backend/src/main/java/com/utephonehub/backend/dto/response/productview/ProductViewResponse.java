package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho hiển thị sản phẩm client-side
 * Dùng cho trang danh sách sản phẩm, tìm kiếm, lọc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductViewResponse {
    
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    
    // Category & Brand info
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    
    // Price range from templates
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Rating & Reviews
    private Double averageRating;
    private Integer totalReviews;
    
    // Stock availability
    private Boolean inStock;
    private Integer totalStock;
    
    // Images
    private List<ProductImageInfo> images;
    
    // Technical Specifications from template & metadata
    private String color;          // from product_templates.color
    private String ram;            // from product_templates.ram
    private String storage;        // from product_templates.storage
    
    // Technical Specifications from metadata (exact DB types)
    private Double screenSize;           // from product_metadata.screen_size (Double - inches)
    private String screenTechnology;     // from product_metadata.screen_technology
    private String cpuChipset;           // from product_metadata.cpu_chipset
    private String operatingSystem;      // from product_metadata.operating_system
    private String cameraDetails;        // from product_metadata.camera_details
    private Double frontCameraMegapixels; // from product_metadata.front_camera_megapixels (Double - MP)
    private Integer batteryCapacity;     // from product_metadata.battery_capacity (Integer - mAh)
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageInfo {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer imageOrder;
    }
}
