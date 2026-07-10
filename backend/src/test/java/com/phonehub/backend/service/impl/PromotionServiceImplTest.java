package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.PromotionRequest;
import com.phonehub.backend.dto.response.PromotionResponse;
import com.phonehub.backend.entity.PromotionTarget;
import com.phonehub.backend.enums.EPromotionTargetType;
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
import static org.mockito.ArgumentMatchers.anyList;
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

    @Test
@DisplayName("Modify promotion - success")
void modifyPromotion_Success() {

    Promotion promotion = new Promotion();
    promotion.setId("P1");

    PromotionTemplate oldTemplate = new PromotionTemplate();
    oldTemplate.setId("OLD");

    PromotionTemplate newTemplate = new PromotionTemplate();
    newTemplate.setId("NEW");

    promotion.setTemplate(oldTemplate);

    PromotionRequest request = new PromotionRequest();
    request.setTemplateId("NEW");
    request.setTitle("New");
    request.setDescription("Desc");
    request.setEffectiveDate(LocalDateTime.now());
    request.setExpirationDate(LocalDateTime.now().plusDays(5));
    request.setPercentDiscount(20.0);
    request.setMinValueToBeApplied(100.0);
    request.setStatus(EPromotionStatus.ACTIVE);
    request.setTargets(new ArrayList<>());

    PromotionResponse response = PromotionResponse.builder().build();

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.of(promotion));

    when(templateRepository.findById("NEW"))
            .thenReturn(Optional.of(newTemplate));

    when(promotionRepository.save(any(Promotion.class)))
            .thenReturn(promotion);

    when(promotionMapper.toResponse(any(Promotion.class)))
            .thenReturn(response);

    PromotionResponse result =
            promotionService.modifyPromotion("P1", request);

    assertNotNull(result);

    verify(targetManager).replaceTargets(any(), any());
    verify(promotionRepository).save(any(Promotion.class));
}
@Test
@DisplayName("Modify promotion - promotion not found")
void modifyPromotion_NotFound() {

    PromotionRequest request = new PromotionRequest();

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.empty());

    assertThrows(
            PromotionNotFoundException.class,
            () -> promotionService.modifyPromotion("P1", request)
    );
}
@Test
@DisplayName("Disable promotion success")
void disable_Success() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.of(promotion));

    promotionService.disable("P1");

    assertEquals(
            EPromotionStatus.INACTIVE,
            promotion.getStatus()
    );

    verify(promotionRepository).save(promotion);

}
@Test
@DisplayName("Disable promotion not found")
void disable_NotFound() {

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.empty());

    assertThrows(
            PromotionNotFoundException.class,
            () -> promotionService.disable("P1")
    );

}
@Test
@DisplayName("Get detail success")
void getDetails_Success() {

    Promotion promotion = new Promotion();

    PromotionResponse response =
            PromotionResponse.builder().build();

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.of(promotion));

    when(promotionMapper.toResponse(promotion))
            .thenReturn(response);

    PromotionResponse result =
            promotionService.getDetails("P1");

    assertNotNull(result);

}
@Test
@DisplayName("Get detail not found")
void getDetails_NotFound() {

    when(promotionRepository.findById("P1"))
            .thenReturn(Optional.empty());

    assertThrows(
            PromotionNotFoundException.class,
            () -> promotionService.getDetails("P1")
    );

}
@Test
void getAllActivePromotions_Success() {

    Promotion promotion = new Promotion();
    promotion.setStatus(EPromotionStatus.ACTIVE);

    PromotionTemplate template = new PromotionTemplate();
    template.setType(EPromotionTemplateType.VOUCHER);

    promotion.setTemplate(template);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));
    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    when(promotionRepository.findAll()).thenReturn(List.of());

    when(promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(any(), any()))
            .thenReturn(List.of(promotion));

    PromotionResponse response =
            PromotionResponse.builder().build();

    when(promotionMapper.toResponse(any()))
            .thenReturn(response);

    List<PromotionResponse> list =
            promotionService.getAllActivePromotions();

    assertEquals(1, list.size());

}
@Test
void getAllActivePromotions_Discount_ShouldNotReturn() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    PromotionTemplate template = new PromotionTemplate();

    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));
    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    when(promotionRepository.findAll()).thenReturn(List.of());

    when(promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(any(), any()))
            .thenReturn(List.of(promotion));

    List<PromotionResponse> result =
            promotionService.getAllActivePromotions();

    assertTrue(result.isEmpty());

}
@Test
void getBestDiscount_ProductTarget() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    promotion.setPercentDiscount(20.0);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));

    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    PromotionTemplate template = new PromotionTemplate();

    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    PromotionTarget target = new PromotionTarget();

    target.setType(EPromotionTargetType.PRODUCT);

    target.setApplicableObjectId(1L);

    promotion.setTargets(List.of(target));

    when(promotionRepository.findAll())
            .thenReturn(List.of(promotion));

    Double result =
            promotionService.getBestDiscountForProduct(1L,2L,3L);

    assertEquals(20.0,result);

}
@Test
void getBestDiscount_CategoryTarget() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    promotion.setPercentDiscount(30.0);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));

    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    PromotionTemplate template = new PromotionTemplate();

    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    PromotionTarget target = new PromotionTarget();

    target.setType(EPromotionTargetType.CATEGORY);

    target.setApplicableObjectId(5L);

    promotion.setTargets(List.of(target));

    when(promotionRepository.findAll())
            .thenReturn(List.of(promotion));

    Double result =
            promotionService.getBestDiscountForProduct(1L,5L,2L);

    assertEquals(30.0,result);

}
@Test
void getBestDiscount_BrandTarget() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    promotion.setPercentDiscount(40.0);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));

    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    PromotionTemplate template = new PromotionTemplate();

    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    PromotionTarget target = new PromotionTarget();

    target.setType(EPromotionTargetType.BRAND);

    target.setApplicableObjectId(8L);

    promotion.setTargets(List.of(target));

    when(promotionRepository.findAll())
            .thenReturn(List.of(promotion));

    Double result =
            promotionService.getBestDiscountForProduct(1L,2L,8L);

    assertEquals(40.0,result);

}
@Test
void getAllActivePromotions_Inactive_ShouldReturnEmpty() {

    Promotion promotion = new Promotion();
    promotion.setStatus(EPromotionStatus.INACTIVE);

    PromotionTemplate template = new PromotionTemplate();
    template.setType(EPromotionTemplateType.VOUCHER);
    promotion.setTemplate(template);

    when(promotionRepository.findAll()).thenReturn(List.of());
    when(promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(any(), any()))
            .thenReturn(List.of(promotion));

    List<PromotionResponse> result = promotionService.getAllActivePromotions();

    assertTrue(result.isEmpty());
}@Test
void checkAndGetAvailablePromotions_Success() {

    Promotion promotion = new Promotion();
    promotion.setStatus(EPromotionStatus.ACTIVE);
    promotion.setMinValueToBeApplied(100.0);

    PromotionTemplate template = new PromotionTemplate();
    template.setType(EPromotionTemplateType.VOUCHER);
    promotion.setTemplate(template);

    when(promotionRepository.findAll()).thenReturn(List.of());

    when(promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(any(), any()))
            .thenReturn(List.of(promotion));

    when(promotionMapper.toResponse(any()))
            .thenReturn(PromotionResponse.builder().build());

    assertEquals(1,
            promotionService.checkAndGetAvailablePromotions(200.0).size());

}
@Test
void checkAndGetAvailablePromotions_MinValueFail() {

    Promotion promotion = new Promotion();
    promotion.setStatus(EPromotionStatus.ACTIVE);
    promotion.setMinValueToBeApplied(500.0);

    PromotionTemplate template = new PromotionTemplate();
    template.setType(EPromotionTemplateType.VOUCHER);
    promotion.setTemplate(template);

    when(promotionRepository.findAll()).thenReturn(List.of());

    when(promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(any(), any()))
            .thenReturn(List.of(promotion));

    assertTrue(
            promotionService
                    .checkAndGetAvailablePromotions(100.0)
                    .isEmpty()
    );

}
@Test
void getBestDiscount_TargetNull() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    promotion.setPercentDiscount(30.0);

    promotion.setTargets(null);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));

    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    PromotionTemplate template = new PromotionTemplate();
    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    when(promotionRepository.findAll())
            .thenReturn(List.of(promotion));

    assertNull(
            promotionService.getBestDiscountForProduct(1L,1L,1L)
    );

}
@Test
void getBestDiscount_ProductWrongId() {

    Promotion promotion = new Promotion();

    promotion.setStatus(EPromotionStatus.ACTIVE);

    promotion.setPercentDiscount(20.0);

    promotion.setEffectiveDate(LocalDateTime.now().minusDays(1));

    promotion.setExpirationDate(LocalDateTime.now().plusDays(1));

    PromotionTemplate template = new PromotionTemplate();

    template.setType(EPromotionTemplateType.DISCOUNT);

    promotion.setTemplate(template);

    PromotionTarget target = new PromotionTarget();

    target.setType(EPromotionTargetType.PRODUCT);

    target.setApplicableObjectId(99L);

    promotion.setTargets(List.of(target));

    when(promotionRepository.findAll())
            .thenReturn(List.of(promotion));

    assertNull(
            promotionService.getBestDiscountForProduct(1L,1L,1L)
    );

}
    
}