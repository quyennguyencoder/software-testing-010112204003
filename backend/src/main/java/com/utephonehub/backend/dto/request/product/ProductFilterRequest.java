package com.utephonehub.backend.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {

    private Long categoryId;
    
    private Long brandId;
    
    private BigDecimal minPrice;
    
    private BigDecimal maxPrice;
    
    private String keyword;
    
    private Boolean activeOnly = true;
}
