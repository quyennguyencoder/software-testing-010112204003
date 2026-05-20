package com.phonehub.backend.exception.promotion;
import com.phonehub.backend.exception.BadRequestException;
/**
 * Exception thrown when a promotion validation fails
 * Follows SOLID principles by creating specific exception types
 */
public class PromotionInvalidException extends BadRequestException {
    
    public PromotionInvalidException(String message) {
        super(message);
    }
}
