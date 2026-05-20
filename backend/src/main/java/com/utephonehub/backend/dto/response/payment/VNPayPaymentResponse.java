package com.utephonehub.backend.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayPaymentResponse {
    
    private String code; // "00" = success
    
    private String message;
    
    private String paymentUrl;
}
