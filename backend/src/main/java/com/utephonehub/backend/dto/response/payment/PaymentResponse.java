package com.utephonehub.backend.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    private Long id;
    
    private Long orderId;
    
    private String paymentMethod; // COD, BANK_TRANSFER, VNPAY
    
    private String provider; // VNPAY, MOMO (chỉ có khi paymentMethod = VNPAY/MOMO)
    
    private String transactionId;
    
    private Long amount;
    
    private String status; // SUCCESS, PENDING, FAILED, CANCELLED
    
    private String createdAt;
}
