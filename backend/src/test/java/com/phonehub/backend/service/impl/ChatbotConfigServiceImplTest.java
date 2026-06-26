package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.phonehub.backend.entity.ChatbotConfig;
import com.phonehub.backend.repository.ChatbotConfigRepository;
import com.phonehub.backend.service.IProductViewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatbotConfigServiceImplTest {

    @Mock
    private ChatbotConfigRepository configRepository;

    @Mock
    private IProductViewService productViewService;

    @InjectMocks
    private ChatbotConfigServiceImpl chatbotConfigService;

    @Test
    @DisplayName("Nên trả về true khi chatbot đang được bật")
    void isChatbotEnabled_ReturnsTrue() {
        ChatbotConfig config = new ChatbotConfig();
        config.setConfigValue("true");
        when(configRepository.findByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)).thenReturn(Optional.of(config));

        assertTrue(chatbotConfigService.isChatbotEnabled());
    }

    @Test
    @DisplayName("Nên thay đổi trạng thái chatbot thành công")
    void toggleChatbot_Success() {
        ChatbotConfig config = new ChatbotConfig();
        config.setConfigValue("true");
        
        when(configRepository.findByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)).thenReturn(Optional.of(config));
        when(configRepository.save(any(ChatbotConfig.class))).thenReturn(config);

        boolean newStatus = chatbotConfigService.toggleChatbot("admin@example.com");

        assertFalse(newStatus);
        verify(configRepository, times(1)).save(any(ChatbotConfig.class));
    }

    @Test
    @DisplayName("Nên tạo fallback response thành công khi gọi AI thất bại")
    void createFallbackResponse_Success() {
        when(productViewService.getFeaturedProducts(anyInt())).thenReturn(new ArrayList<>());
        when(productViewService.getNewArrivals(anyInt())).thenReturn(new ArrayList<>());
        when(productViewService.getBestSellingProducts(anyInt())).thenReturn(new ArrayList<>());

        ChatbotAssistantUserResponse response = chatbotConfigService.createFallbackResponse();

        assertNotNull(response);
        assertEquals("FALLBACK_MODE", response.getDetectedIntent());
        assertNotNull(response.getAiResponse());
    }

    @Test
    @DisplayName("Nên xử lý lỗi gọn gàng khi tạo fallback response gặp lỗi hệ thống")
    void createFallbackResponse_HandlesError() {
        when(productViewService.getFeaturedProducts(anyInt())).thenThrow(new RuntimeException("DB Error"));

        ChatbotAssistantUserResponse response = chatbotConfigService.createFallbackResponse();

        assertNotNull(response);
        assertEquals("FALLBACK_ERROR", response.getDetectedIntent());
        assertTrue(response.getAiResponse().contains("hệ thống đang bận"));
    }
}
