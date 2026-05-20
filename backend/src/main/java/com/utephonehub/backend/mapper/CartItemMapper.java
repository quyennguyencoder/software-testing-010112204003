package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.cart.CartItemResponse;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.ProductTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Mapper for CartItem entity to CartItemResponse DTO
 * Updated to calculate price and stock from ProductTemplate entities
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumbnailUrl", source = "product.thumbnailUrl")
    @Mapping(target = "unitPrice", expression = "java(getCheapestPrice(cartItem))")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cartItem))")
    @Mapping(target = "stockQuantity", expression = "java(getTotalStock(cartItem))")
    @Mapping(target = "outOfStock", expression = "java(isOutOfStock(cartItem))")
    @Mapping(target = "overStock", expression = "java(isOverStock(cartItem))")
    CartItemResponse toResponse(CartItem cartItem);

    /**
     * Get cheapest price from active product templates
     */
    default BigDecimal getCheapestPrice(CartItem cartItem) {
        return cartItem.getProduct().getTemplates().stream()
                .filter(ProductTemplate::getStatus)
                .map(ProductTemplate::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get total stock from all active product templates
     */
    default Integer getTotalStock(CartItem cartItem) {
        return cartItem.getProduct().getTemplates().stream()
                .filter(ProductTemplate::getStatus)
                .mapToInt(ProductTemplate::getStockQuantity)
                .sum();
    }

    /**
     * Calculate subtotal = unitPrice * quantity
     */
    default BigDecimal calculateSubtotal(CartItem cartItem) {
        BigDecimal price = getCheapestPrice(cartItem);
        BigDecimal quantity = BigDecimal.valueOf(cartItem.getQuantity());
        return price.multiply(quantity);
    }

    /**
     * Check if product is out of stock (all templates have 0 stock)
     */
    default boolean isOutOfStock(CartItem cartItem) {
        Integer totalStock = getTotalStock(cartItem);
        return totalStock == 0;
    }

    /**
     * Check if cart quantity exceeds available stock
     */
    default boolean isOverStock(CartItem cartItem) {
        Integer totalStock = getTotalStock(cartItem);
        return cartItem.getQuantity() > totalStock;
    }
}
