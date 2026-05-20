package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.order.OrderItemResponse;
import com.utephonehub.backend.dto.response.order.OrderResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    /**
     * Convert Order entity sang OrderResponse DTO
     * @param order Order entity
     * @return OrderResponse DTO
     */
    @Mapping(source = "promotion.template.code", target = "promotionCode")
    @Mapping(source = "items", target = "items")
    OrderResponse toOrderResponse(Order order);
    
    /**
     * Convert OrderItem entity sang OrderItemResponse DTO
     * @param item OrderItem entity
     * @return OrderItemResponse DTO
     */
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    OrderItemResponse toOrderItemResponse(OrderItem item);
    
    /**
     * Helper method: Tính subtotal = price * quantity
     */
    default BigDecimal calculateSubtotal(OrderItem item) {
        return item.getPrice().multiply(new BigDecimal(item.getQuantity()));
    }
}