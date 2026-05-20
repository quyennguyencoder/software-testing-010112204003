package com.utephonehub.backend.dto.response.product;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    private Long id;
    
    private String imageUrl;
    
    private String altText;
    
    private Boolean isPrimary;
    
    private Integer imageOrder;
    
    private LocalDateTime createdAt;
}
