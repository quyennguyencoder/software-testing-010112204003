package com.utephonehub.backend.exception.promotion;
import com.utephonehub.backend.exception.ResourceNotFoundException;
/**
 * Exception thrown when a promotion is not found
 * Follows SOLID principles by creating specific exception types
 */
public class PromotionNotFoundException extends ResourceNotFoundException {
    
    public PromotionNotFoundException(String promotionId) {
        super(String.format("Promotion not found with ID: %s", promotionId));
    }
}
