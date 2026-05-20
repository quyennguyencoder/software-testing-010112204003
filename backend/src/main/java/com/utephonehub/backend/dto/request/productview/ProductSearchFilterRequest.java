package com.utephonehub.backend.dto.request.productview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho tìm kiếm sản phẩm theo từ khóa
 * Chỉ hỗ trợ search keyword, không có filter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request tìm kiếm sản phẩm theo từ khóa")
public class ProductSearchFilterRequest {
    
    @Schema(description = "Từ khóa tìm kiếm (tên sản phẩm)", example = "iPhone 15")
    private String keyword;
    
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
