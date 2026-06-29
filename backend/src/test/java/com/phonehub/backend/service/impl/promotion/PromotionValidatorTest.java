package com.phonehub.backend.service.impl.promotion;

import com.phonehub.backend.entity.Promotion;
import com.phonehub.backend.enums.EPromotionStatus;
import com.phonehub.backend.exception.promotion.PromotionInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PromotionValidatorTest {

    @InjectMocks
    private PromotionValidator validator;

    private Promotion promotion;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
        promotion.setStatus(EPromotionStatus.ACTIVE);
        promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));
        promotion.setExpirationDate(LocalDateTime.now().plusDays(1));
        promotion.setMinValueToBeApplied(100.0);
    }

    @Test
    void validatePromotionApplicability_Valid_DoesNotThrow() {
        assertDoesNotThrow(() -> validator.validatePromotionApplicability(promotion, 150.0));
    }

    @Test
    void validatePromotionActive_Inactive_ThrowsException() {
        promotion.setStatus(EPromotionStatus.INACTIVE);
        
        assertThrows(PromotionInvalidException.class, () -> validator.validatePromotionActive(promotion));
    }

    @Test
    void validatePromotionDateRange_NotStarted_ThrowsException() {
        promotion.setEffectiveDate(LocalDateTime.now().plusDays(1));
        
        assertThrows(PromotionInvalidException.class, () -> validator.validatePromotionDateRange(promotion));
    }

    @Test
    void validatePromotionDateRange_Expired_ThrowsException() {
        promotion.setExpirationDate(LocalDateTime.now().minusDays(1));
        
        assertThrows(PromotionInvalidException.class, () -> validator.validatePromotionDateRange(promotion));
    }

    @Test
    void validateMinimumOrderValue_BelowMinimum_ThrowsException() {
        assertThrows(PromotionInvalidException.class, () -> validator.validateMinimumOrderValue(promotion, 50.0));
    }

    @Test
    void isPromotionValid_Valid_ReturnsTrue() {
        assertTrue(validator.isPromotionValid(promotion, 150.0));
    }

    @Test
    void isPromotionValid_Invalid_ReturnsFalse() {
        assertFalse(validator.isPromotionValid(promotion, 50.0));
    }
}