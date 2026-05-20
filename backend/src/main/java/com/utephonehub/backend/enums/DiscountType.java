package com.utephonehub.backend.enums;

/**
 * Discount type enumeration
 */
public enum DiscountType {
    PERCENTAGE,    // Giảm theo %
    PERCENT,       // Alias cho PERCENTAGE (backward compatible)
    AMOUNT,        // Giảm theo số tiền cố định
    FIXED_AMOUNT   // Alias cho AMOUNT (backward compatible)
}
