package com.utephonehub.backend.dto.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to update guest cart item quantity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuestCartItemRequest {

    @NotNull(message = "Session ID không được để trống")
    private String sessionId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer quantity;
}
