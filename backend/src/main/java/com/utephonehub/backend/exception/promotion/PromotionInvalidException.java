package com.utephonehub.backend.exception.promotion;
import com.utephonehub.backend.exception.BadRequestException;
/**
 * Exception thrown when a promotion validation fails
 * Follows SOLID principles by creating specific exception types
 */
public class PromotionInvalidException extends BadRequestException {
    
    public PromotionInvalidException(String message) {
        super(message);
    }
}
