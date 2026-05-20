package com.utephonehub.backend.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for guest cart operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestCartRequest {

    @NotNull(message = "Session ID không được để trống")
    private String sessionId;

    @NotNull(message = "Product ID không được để trống")
    private Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
}
