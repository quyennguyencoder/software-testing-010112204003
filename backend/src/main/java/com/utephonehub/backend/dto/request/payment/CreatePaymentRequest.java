package com.utephonehub.backend.dto.request.payment;

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
public class CreatePaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 10000, message = "Minimum amount is 10,000 VND")
    private Long amount;
    
    private String orderInfo;
    
    private String locale; // "vn" or "en"
}
