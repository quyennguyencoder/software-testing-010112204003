
package com.utephonehub.backend.dto.request.order;

import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin order filter request")
public class AdminOrderFilterRequest {
    
    @Schema(description = "Search keyword (order code, email, phone, recipient name)", example = "ORD-001")
    private String search;
    
    @Schema(description = "Filter by order status", example = "PENDING")
    private OrderStatus status;
    
    @Schema(description = "Filter by payment method", example = "COD")
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Filter by customer ID", example = "2")
    private Long customerId;
    
    @Schema(description = "Filter by customer email", example = "huong.tran@gmail.com")
    private String customerEmail;
    
    @Schema(description = "Filter orders from date (YYYY-MM-DD)", example = "2025-12-01")
    private LocalDate fromDate;
    
    @Schema(description = "Filter orders to date (YYYY-MM-DD)", example = "2025-12-31")
    private LocalDate toDate;
    
    @Schema(description = "Filter orders with total amount >= this value", example = "1000000")
    private BigDecimal minAmount;
    
    @Schema(description = "Filter orders with total amount <= this value", example = "50000000")
    private BigDecimal maxAmount;
    
    @Schema(description = "Sort field", example = "createdAt", allowableValues = {"createdAt", "updatedAt", "totalAmount", "orderCode"})
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";
}