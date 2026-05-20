package com.phonehub.backend.dto.response.order;

import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderCode;
    private String email;
    private String recipientName;
    private String phoneNumber;
    private String shippingAddress;
    private BigDecimal shippingFee;
    private String shippingUnit;
    private String note;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String promotionCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;

}