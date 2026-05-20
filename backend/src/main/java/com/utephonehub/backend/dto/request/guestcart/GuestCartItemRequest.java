package com.utephonehub.backend.dto.request.guestcart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCartItemRequest {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "quantity không được để trống")
    @Min(value = 1, message = "quantity tối thiểu là 1")
    @Max(value = 10, message = "quantity tối đa là 10")
    private Integer quantity;
}
