package com.utephonehub.backend.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartUpdatedEvent {
    
    private Long cartId;
    private Long userId;
    private String eventType; // ADDED, UPDATED, REMOVED, CLEARED
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private LocalDateTime timestamp;
}
