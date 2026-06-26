package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.PromotionTemplateRequest;
import com.phonehub.backend.dto.response.PromotionTemplateResponse;
import com.phonehub.backend.entity.Promotion;
import com.phonehub.backend.entity.PromotionTemplate;
import com.phonehub.backend.enums.EPromotionTemplateType;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.PromotionTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionTemplateServiceImplTest {

    @Mock
    private PromotionTemplateRepository templateRepository;

    @InjectMocks
    private PromotionTemplateServiceImpl templateService;

    @Test
    @DisplayName("Nên tạo template khuyến mãi thành công")
    void createTemplate_Success() {
        PromotionTemplateRequest request = new PromotionTemplateRequest();
        request.setCode("SUMMER_SALE");
        request.setType(EPromotionTemplateType.VOUCHER);

        PromotionTemplate savedTemplate = new PromotionTemplate();
        savedTemplate.setId("TPL123");
        savedTemplate.setCode("SUMMER_SALE");
        savedTemplate.setType(EPromotionTemplateType.VOUCHER);

        when(templateRepository.save(any(PromotionTemplate.class))).thenReturn(savedTemplate);

        PromotionTemplateResponse response = templateService.createTemplate(request);

        assertNotNull(response);
        assertEquals("TPL123", response.getId());
        assertEquals("SUMMER_SALE", response.getCode());
        assertEquals(EPromotionTemplateType.VOUCHER, response.getType());
    }

    @Test
    @DisplayName("Nên cập nhật template thành công")
    void updateTemplate_Success() {
        PromotionTemplateRequest request = new PromotionTemplateRequest();
        request.setCode("WINTER_SALE");
        request.setType(EPromotionTemplateType.DISCOUNT);

        PromotionTemplate existingTemplate = new PromotionTemplate();
        existingTemplate.setId("TPL123");

        when(templateRepository.findById("TPL123")).thenReturn(Optional.of(existingTemplate));
        when(templateRepository.save(any(PromotionTemplate.class))).thenReturn(existingTemplate);

        PromotionTemplateResponse response = templateService.updateTemplate("TPL123", request);

        assertNotNull(response);
        verify(templateRepository, times(1)).save(existingTemplate);
    }

    @Test
    @DisplayName("Nên ném lỗi ResourceNotFoundException khi không tìm thấy template")
    void updateTemplate_ThrowsException_WhenNotFound() {
        PromotionTemplateRequest request = new PromotionTemplateRequest();
        when(templateRepository.findById("TPL123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> templateService.updateTemplate("TPL123", request));
    }

    @Test
    @DisplayName("Nên xóa template thành công khi không có promotion nào sử dụng")
    void deleteTemplate_Success() {
        PromotionTemplate template = new PromotionTemplate();
        template.setId("TPL123");
        template.setPromotions(new ArrayList<>()); // No promotions

        when(templateRepository.findById("TPL123")).thenReturn(Optional.of(template));
        doNothing().when(templateRepository).delete(template);

        templateService.deleteTemplate("TPL123");

        verify(templateRepository, times(1)).delete(template);
    }

    @Test
    @DisplayName("Nên ném lỗi IllegalStateException khi xóa template đang được sử dụng")
    void deleteTemplate_ThrowsException_WhenInUse() {
        PromotionTemplate template = new PromotionTemplate();
        template.setId("TPL123");
        template.setPromotions(List.of(new Promotion())); // In use

        when(templateRepository.findById("TPL123")).thenReturn(Optional.of(template));

        assertThrows(IllegalStateException.class, () -> templateService.deleteTemplate("TPL123"));
        verify(templateRepository, never()).delete(any());
    }
}