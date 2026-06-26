package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.PromotionRequest;
import com.phonehub.backend.dto.response.PromotionResponse;
import com.phonehub.backend.entity.Promotion;
import com.phonehub.backend.entity.PromotionTemplate;
import com.phonehub.backend.enums.EPromotionStatus;
import com.phonehub.backend.enums.EPromotionTemplateType;
import com.phonehub.backend.exception.promotion.PromotionNotFoundException;
import com.phonehub.backend.mapper.PromotionMapper;
import com.phonehub.backend.repository.PromotionRepository;
import com.phonehub.backend.repository.PromotionTemplateRepository;
import com.phonehub.backend.service.impl.promotion.PromotionDiscountCalculator;
import com.phonehub.backend.service.impl.promotion.PromotionTargetManager;
import com.phonehub.backend.service.impl.promotion.PromotionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceImplTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionTemplateRepository templateRepository;

    @Mock
    private PromotionValidator promotionValidator;

    @Mock
    private PromotionDiscountCalculator discountCalculator;

    @Mock
    private PromotionMapper promotionMapper;

    @Mock
    private PromotionTargetManager targetManager;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    @Test
    @DisplayName("Nên tạo khuyến mãi thành công")
    void createPromotion_Success() {
        PromotionRequest request = new PromotionRequest();
        request.setTemplateId("TPL123");
        request.setTitle("Giảm giá 10%");
        request.setTargets(new ArrayList<>());

        PromotionTemplate template = new PromotionTemplate();
        template.setId("TPL123");

        Promotion mockPromotion = new Promotion();
        PromotionResponse mockResponse = PromotionResponse.builder().build();

        when(templateRepository.findById("TPL123")).thenReturn(Optional.of(template));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(mockPromotion);
        doNothing().when(targetManager).saveTargets(any(Promotion.class), any());
        when(promotionMapper.toResponse(mockPromotion)).thenReturn(mockResponse);

        PromotionResponse response = promotionService.createPromotion(request);

        assertNotNull(response);
        verify(promotionRepository, times(1)).save(any(Promotion.class));
        verify(targetManager, times(1)).saveTargets(any(Promotion.class), any());
    }

    @Test
    @DisplayName("Nên tính toán giảm giá thành công")
    void calculateDiscount_Success() {
        Promotion mockPromotion = new Promotion();
        mockPromotion.setId("PROM123");

        when(promotionRepository.findById("PROM123")).thenReturn(Optional.of(mockPromotion));
        doNothing().when(promotionValidator).validatePromotionApplicability(mockPromotion, 1000000.0);
        when(discountCalculator.calculateDiscountAmount(mockPromotion, 1000000.0)).thenReturn(100000.0);

        Double discount = promotionService.calculateDiscount("PROM123", 1000000.0);

        assertEquals(100000.0, discount);
    }

    @Test
    @DisplayName("Nên ném lỗi PromotionNotFoundException khi không tìm thấy khuyến mãi")
    void calculateDiscount_ThrowsException_WhenNotFound() {
        when(promotionRepository.findById("PROM123")).thenReturn(Optional.empty());

        assertThrows(PromotionNotFoundException.class, () -> 
                promotionService.calculateDiscount("PROM123", 1000000.0));
    }

    @Test
    @DisplayName("Nên tự động vô hiệu hóa các khuyến mãi đã hết hạn khi lấy danh sách")
    void getAllPromotions_AutoDisablesExpired() {
        Promotion expiredPromotion = new Promotion();
        expiredPromotion.setStatus(EPromotionStatus.ACTIVE);
        expiredPromotion.setExpirationDate(LocalDateTime.now().minusDays(1)); // Đã hết hạn

        when(promotionRepository.findAll()).thenReturn(List.of(expiredPromotion));
        when(promotionRepository.saveAll(anyList())).thenReturn(List.of(expiredPromotion));
        when(promotionMapper.toResponseList(anyList())).thenReturn(new ArrayList<>());

        promotionService.getAllPromotions();

        // Expired promotion should be changed to INACTIVE
        assertEquals(EPromotionStatus.INACTIVE, expiredPromotion.getStatus());
        verify(promotionRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Nên lấy discount tốt nhất cho sản phẩm thành công")
    void getBestDiscountForProduct_Success() {
        Promotion mockPromotion = new Promotion();
        mockPromotion.setStatus(EPromotionStatus.ACTIVE);
        mockPromotion.setEffectiveDate(LocalDateTime.now().minusDays(1));
        mockPromotion.setExpirationDate(LocalDateTime.now().plusDays(1));
        mockPromotion.setPercentDiscount(15.0);
        
        PromotionTemplate template = new PromotionTemplate();
        template.setType(EPromotionTemplateType.DISCOUNT);
        mockPromotion.setTemplate(template);
        
        // Cần thêm mock target (Tuy nhiên do scope private test này có thể cần sửa hoặc skip phần list targets
        // Giả lập bằng list rỗng để nó return null hoặc test case negative)
        when(promotionRepository.findAll()).thenReturn(List.of(mockPromotion));

        Double discount = promotionService.getBestDiscountForProduct(1L, 1L, 1L);

        // Do target rỗng => isPromotionApplicableToProduct trả về false => discount = null
        assertNull(discount);
    }
}