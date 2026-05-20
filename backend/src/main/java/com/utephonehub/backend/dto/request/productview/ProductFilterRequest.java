package com.utephonehub.backend.dto.request.productview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO cho lọc sản phẩm đa tiêu chí
 * Hỗ trợ kết hợp nhiều filter cùng lúc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request lọc sản phẩm đa tiêu chí")
public class ProductFilterRequest {
    
    // Danh mục và thương hiệu
    @Schema(description = "Danh sách ID danh mục", example = "[1, 2, 3]")
    private List<Long> categoryIds;
    
    @Schema(description = "Danh sách ID thương hiệu", example = "[1, 2, 3]")
    private List<Long> brandIds;
    
    // Khoảng giá (thanh trượt)
    @Schema(description = "Giá tối thiểu", example = "5000000")
    private BigDecimal minPrice;
    
    @Schema(description = "Giá tối đa", example = "30000000")
    private BigDecimal maxPrice;
    
    // Technical Specifications
    @Schema(description = "Danh sách RAM (GB)", example = "[\"4GB\", \"6GB\", \"8GB\", \"12GB\"]")
    private List<String> ramOptions;
    
    @Schema(description = "Danh sách bộ nhớ trong (GB)", example = "[\"64GB\", \"128GB\", \"256GB\", \"512GB\"]")
    private List<String> storageOptions;
    
    @Schema(description = "Dung lượng pin tối thiểu (mAh)", example = "4000")
    private Integer minBattery;
    
    @Schema(description = "Dung lượng pin tối đa (mAh)", example = "6000")
    private Integer maxBattery;
    
    @Schema(description = "Danh sách kích thước màn hình", example = "[\"6.1\", \"6.7\"]")
    private List<String> screenSizeOptions;
    
    @Schema(description = "Danh sách hệ điều hành", example = "[\"iOS\", \"Android\"]")
    private List<String> osOptions;
    
    // Đánh giá và trạng thái
    @Schema(description = "Đánh giá tối thiểu (1.0-5.0)", example = "4.0")
    private Double minRating;
    
    @Schema(description = "Đánh giá tối đa (1.0-5.0)", example = "4.5")
    private Double maxRating;
    
    @Schema(description = "Chỉ hiển thị sản phẩm còn hàng", example = "true")
    private Boolean inStockOnly;
    
    @Schema(description = "Chỉ hiển thị sản phẩm có khuyến mãi", example = "true")  
    private Boolean hasDiscountOnly;
    
    // Sorting và Pagination
    @Schema(description = "Sắp xếp theo (name, price, rating, created_date)", example = "price")
    private String sortBy;
    
    @Schema(description = "Hướng sắp xếp (asc, desc)", example = "asc")
    private String sortDirection;
    
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0")
    @Builder.Default
    private Integer page = 0;
    
    @Schema(description = "Số sản phẩm mỗi trang", example = "20")
    @Builder.Default
    private Integer size = 20;
}