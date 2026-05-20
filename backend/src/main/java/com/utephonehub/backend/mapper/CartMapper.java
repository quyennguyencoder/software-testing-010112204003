package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.cart.CartItemResponse;
import com.utephonehub.backend.dto.response.cart.CartResponse;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.ProductTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Mapper for Cart entity to CartResponse DTO
 * Updated to calculate price from ProductTemplate entities
 */
@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(cart))")
    @Mapping(target = "itemCount", expression = "java(calculateItemCount(cart))")
    CartResponse toResponse(Cart cart);

    /**
     * Calculate total amount = sum of all item subtotals
     * Price is retrieved from cheapest active template of each product
     */
    default BigDecimal calculateTotalAmount(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return cart.getItems().stream()
                .map(item -> {
                    // Get cheapest price from active templates
                    BigDecimal price = item.getProduct().getTemplates().stream()
                            .filter(ProductTemplate::getStatus)
                            .map(ProductTemplate::getPrice)
                            .min(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                    return price.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total item count in cart
     */
    default Integer calculateItemCount(Cart cart) {
        if (cart.getItems() == null) {
            return 0;
        }
        return cart.getItems().size();
    }
}
