package com.utephonehub.backend.dto.response.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPaymentResponse {
    
    private Long id;
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String customerEmail;
    private String paymentMethod;
    private String provider;
    private String transactionId;
    private Long amount;
    private String status;
    private Boolean reconciled;
    private String note;
    private String createdAt;
}
