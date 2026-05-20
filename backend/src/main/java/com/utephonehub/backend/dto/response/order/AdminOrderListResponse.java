
package com. utephonehub.backend. dto.response.order;

import com.utephonehub. backend.entity.Order;
import com.utephonehub.backend.enums. OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Admin order list item response")
public class AdminOrderListResponse {
    
    @Schema(description = "Order ID", example = "1")
    private Long id;
    
    @Schema(description = "Order code", example = "ORD-001")
    private String orderCode;
    
    @Schema(description = "Customer ID", example = "2")
    private Long customerId;
    
    @Schema(description = "Customer name", example = "Trần Thị Hương")
    private String customerName;
    
    @Schema(description = "Customer email", example = "huong.tran@gmail. com")
    private String customerEmail;
    
    @Schema(description = "Customer phone", example = "0912345678")
    private String customerPhone;
    
    @Schema(description = "Recipient name", example = "Trần Thị Hương")
    private String recipientName;
    
    @Schema(description = "Recipient phone", example = "0912345678")
    private String recipientPhone;
    
    @Schema(description = "Order status", example = "DELIVERED")
    private OrderStatus status;
    
    @Schema(description = "Status display name", example = "Đã giao hàng")
    private String statusDisplay;
    
    @Schema(description = "Payment method", example = "COD")
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Total amount", example = "32990000.00")
    private BigDecimal totalAmount;
    
    @Schema(description = "Shipping fee", example = "30000.00")
    private BigDecimal shippingFee;
    
    @Schema(description = "Shipping address", example = "123 Lê Lợi, TP. HCM")
    private String shippingAddress;
    
    @Schema(description = "Order created date", example = "2025-12-09T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Order last updated date", example = "2025-12-11T14:20:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Number of items in order", example = "3")
    private Integer itemCount;
    
    @Schema(description = "Order note", example = "Giao hàng giờ hành chính")
    private String note;
    
    public static AdminOrderListResponse fromEntity(Order order) {
        return AdminOrderListResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getUser() != null ? order.getUser().getFullName() : "Guest")
                .customerEmail(order.getEmail())
                .customerPhone(order.getUser() != null ? order.getUser().getPhoneNumber() : null)
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getPhoneNumber())
                .status(order.getStatus())
                .statusDisplay(getStatusDisplayName(order.getStatus()))
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .itemCount(order.getItems() != null ? order.getItems().size() : 0)
                .note(order.getNote())
                .build();
    }
    
    private static String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELLED -> "Đã hủy";
        };
    }
}