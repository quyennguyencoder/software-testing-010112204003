package com.phonehub.backend.service.impl.promotion;

import com.phonehub.backend.entity.Promotion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PromotionDiscountCalculatorTest {

    @InjectMocks
    private PromotionDiscountCalculator calculator;

    private Promotion promotion;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
    }

    @Test
    void calculateDiscountAmount_FixedAmount_ReturnsFixedAmount() {
        promotion.setFixedAmount(50.0);
        
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(50.0, discount);
    }

    @Test
    void calculateDiscountAmount_FixedAmountGreaterThanTotal_ReturnsTotal() {
        promotion.setFixedAmount(150.0);
        
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(100.0, discount);
    }

    @Test
    void calculateDiscountAmount_PercentageAmount_ReturnsPercentage() {
        promotion.setPercentDiscount(20.0);
        
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(20.0, discount);
    }

    @Test
    void calculateDiscountAmount_PercentageWithMaxCap_ReturnsMaxCap() {
        promotion.setPercentDiscount(20.0);
        promotion.setMaxDiscount(15.0);
        
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(15.0, discount); // 20% of 100 is 20, but max cap is 15
    }

    @Test
    void calculateDiscountAmount_PercentageWithoutMaxCap_ReturnsCalculated() {
        promotion.setPercentDiscount(20.0);
        promotion.setMaxDiscount(50.0);
        
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(20.0, discount); // 20% of 100 is 20, max cap is 50, so return 20
    }

    @Test
    void calculateDiscountAmount_NoDiscountSpecified_ReturnsZero() {
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        
        assertEquals(0.0, discount);
    }

    @Test
    void calculatePercentageDiscount_InvalidInputs_ReturnsZero() {
        promotion.setPercentDiscount(-10.0);
        Double discount = calculator.calculateDiscountAmount(promotion, 100.0);
        assertEquals(0.0, discount);
        
        promotion.setPercentDiscount(150.0); // handled by validPercentage in private method
        discount = calculator.calculateDiscountAmount(promotion, 100.0);
        assertEquals(100.0, discount); // 100% of 100 is 100
    }
}