package com.utephonehub.backend.service.impl.promotion;

import com.utephonehub.backend.entity.Promotion;
import org.springframework.stereotype.Component;

/**
 * Calculator for promotion discounts
 * Follows Single Responsibility Principle (SRP) - only calculates discounts
 * Follows Open/Closed Principle (OCP) - can be extended for new discount strategies
 * Follows Information Expert (GRASP) - knows discount calculation rules
 */
@Component
public class PromotionDiscountCalculator {

    /**
     * Calculate discount amount for a promotion
     * @param promotion Promotion to apply
     * @param orderTotal Original order total
     * @return Discount amount
     */
    public Double calculateDiscountAmount(Promotion promotion, Double orderTotal) {
        // Priority 1: Fixed amount discount
        if (promotion.getFixedAmount() != null && promotion.getFixedAmount() > 0) {
            return Math.min(promotion.getFixedAmount(), orderTotal);
        }
        
        // Priority 2: Percentage-based discount
        if (promotion.getPercentDiscount() != null && promotion.getPercentDiscount() > 0) {
            Double discount = calculatePercentageDiscount(orderTotal, promotion.getPercentDiscount());
            
            // Apply max discount cap if exists
            if (promotion.getMaxDiscount() != null && promotion.getMaxDiscount() > 0) {
                discount = Math.min(discount, promotion.getMaxDiscount());
            }
            
            return discount;
        }
        
        return 0.0;
    }

    /**
     * Calculate percentage-based discount
     * @param amount Original amount
     * @param percentage Discount percentage (0-100)
     * @return Discount amount
     */
    private Double calculatePercentageDiscount(Double amount, Double percentage) {
        if (amount == null || percentage == null || amount <= 0 || percentage <= 0) {
            return 0.0;
        }
        
        // Ensure percentage is within valid range
        double validPercentage = Math.min(100.0, Math.max(0.0, percentage));
        return amount * (validPercentage / 100.0);
    }
}
