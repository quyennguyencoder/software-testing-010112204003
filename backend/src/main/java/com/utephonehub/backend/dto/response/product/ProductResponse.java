package com.utephonehub.backend.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private Integer stockQuantity;
    
    private String thumbnailUrl;
    
    private String specifications;
    
    private Boolean status;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Long brandId;
    
    private String brandName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
