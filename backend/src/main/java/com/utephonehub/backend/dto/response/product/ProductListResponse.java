package com.utephonehub.backend.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Product in list/table view
 * Shows simplified info with price/stock from cheapest/default template
 * Aligned with Usecase M02: Table shows Ảnh | Tên | SKU | Giá | Tồn kho | Status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {

    private Long id;
    
    private String name;
    
    /**
     * Price of cheapest template (for sorting/filtering)
     * Calculated from ProductTemplate with MIN(price)
     */
    private BigDecimal price;
    
    /**
     * Discount percentage from active DISCOUNT promotions (0-100)
     * Automatically calculated based on PRODUCT/CATEGORY/BRAND targets
     * Null if no discount applies
     */
    private Double discountPercent;
    
    /**
     * Price after discount applied
     * Calculated as: price * (1 - discountPercent/100)
     * Same as price if no discount
     */
    private BigDecimal discountedPrice;
    
    /**
     * Total stock quantity across all templates
     * Calculated from SUM(ProductTemplate.stockQuantity)
     */
    private Integer stockQuantity;
    
    private String thumbnailUrl;
    
    private Boolean status;
    
    private String categoryName;
    
    private String brandName;
    
    /**
     * Number of product images
     */
    private Integer imageCount;
    
    /**
     * Product images (for image management modal)
     */
    private List<ProductImageResponse> images;
}
