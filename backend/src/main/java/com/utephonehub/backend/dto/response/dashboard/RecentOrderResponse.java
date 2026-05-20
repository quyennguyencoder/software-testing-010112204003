package com.utephonehub.backend.dto.response.dashboard;

import com.utephonehub.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Recent Orders
 * Used for dashboard recent orders list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderResponse {
    
    /**
     * Order ID
     */
    private Long orderId;
    
    /**
     * Customer name (full name)
     */
    private String customerName;
    
    /**
     * Customer email
     */
    private String customerEmail;
    
    /**
     * Total order amount
     */
    private BigDecimal totalAmount;
    
    /**
     * Order status
     */
    private OrderStatus status;
    
    /**
     * Vietnamese status label
     */
    private String statusLabel;
    
    /**
     * Order created date/time
     */
    private LocalDateTime createdAt;
}
