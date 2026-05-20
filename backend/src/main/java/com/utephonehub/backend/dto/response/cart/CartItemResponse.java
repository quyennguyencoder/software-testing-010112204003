package com.utephonehub.backend.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productThumbnailUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stockQuantity;
    private Boolean outOfStock;
    private Boolean overStock;
}
