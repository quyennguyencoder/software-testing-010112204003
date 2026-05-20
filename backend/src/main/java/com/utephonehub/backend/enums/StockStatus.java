package com.utephonehub.backend.enums;

/**
 * Enum representing product template stock status
 * Aligned with Class Diagram requirements
 */
public enum StockStatus {
    /**
     * Product is in stock (quantity > 10)
     */
    IN_STOCK,
    
    /**
     * Product stock is low (1 <= quantity <= 10)
     */
    LOW_STOCK,
    
    /**
     * Product is out of stock (quantity = 0)
     */
    OUT_OF_STOCK
}
