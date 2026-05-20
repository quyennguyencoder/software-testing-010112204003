package com.utephonehub.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho chatbot tư vấn sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAssistantUserRequest {
    
    /**
     * Câu hỏi/yêu cầu từ khách hàng
     */
    private String message;
    
    /**
     * ID sản phẩm (tùy chọn, để lấy sản phẩm liên quan)
     */
    private Long productId;
    
    /**
     * ID danh mục (tùy chọn, để lọc theo danh mục)
     */
    private Long categoryId;
    
    /**
     * Giá min (tùy chọn)
     */
    private Double minPrice;
    
    /**
     * Giá max (tùy chọn)
     */
    private Double maxPrice;
    
    /**
     * Sắp xếp theo: RELEVANCE, PRICE_ASC, PRICE_DESC, NEWEST, BEST_SELLING
     */
    private String sortBy;
}
