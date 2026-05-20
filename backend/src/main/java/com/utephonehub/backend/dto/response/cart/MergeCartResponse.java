package com.utephonehub.backend.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeCartResponse {
    
    private CartResponse cart;
    private Integer mergedItemsCount;
    private Integer skippedItemsCount;
    private String message;
}
