package com.utephonehub.backend.dto.response.order;

import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    
    private Long orderId;
    private String orderCode;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private String message;
    
    // Nếu thanh toán VNPay, sẽ có paymentUrl
    private String paymentUrl;
}
