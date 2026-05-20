package com.utephonehub.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utephonehub.backend.dto.request.ChatbotAssistantUserRequest;
import com.utephonehub.backend.dto.response.ChatbotAssistantUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test cho ChatbotAssistantUserService
 * Kiểm tra:
 * - Intent detection (phân loại câu hỏi)
 * - Product recommendations (lấy sản phẩm)
 * - AI response generation (tạo phản hồi)
 */
@SpringBootTest
class ChatbotAssistantUserServiceTest {

    @Autowired
    private IChatbotAssistantUserService chatbotService;

    @MockBean
    private IProductRecommendationService productRecommendationService;

    @MockBean
    private IGeminiFallbackService fallbackService;

    @MockBean
    private IGeminiEmbeddingService embeddingService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<ChatbotAssistantUserResponse.RecommendedProductDTO> mockProducts;

    @BeforeEach
    void setUp() {
        // Mock sản phẩm
        mockProducts = new ArrayList<>();
        
        ChatbotAssistantUserResponse.RecommendedProductDTO product1 = 
            ChatbotAssistantUserResponse.RecommendedProductDTO.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Flagship smartphone với camera tuyệt vời")
                .price(25000000.0)
                .rating(4.8)
                .reviewCount(150)
                .imageUrl("/images/iphone15pro.jpg")
                .categoryName("Smartphones")
                .productUrl("/products/1")
                .matchScore(0.95)
                .reason("Phù hợp với nhu cầu máy ảnh tốt")
                .build();
        
        ChatbotAssistantUserResponse.RecommendedProductDTO product2 = 
            ChatbotAssistantUserResponse.RecommendedProductDTO.builder()
                .id(2L)
                .name("Samsung Galaxy S24 Ultra")
                .description("Flagship điện thoại cao cấp nhất")
                .price(26000000.0)
                .rating(4.7)
                .reviewCount(200)
                .imageUrl("/images/galaxy-s24.jpg")
                .categoryName("Smartphones")
                .productUrl("/products/2")
                .matchScore(0.93)
                .reason("Camera cực nét, hiệu suất cao")
                .build();
        
        mockProducts.add(product1);
        mockProducts.add(product2);
    }

    /**
     * Test: Lấy sản phẩm nổi bật
     */
    @Test
    void testChatWithFeaturedProductsIntent() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Tôi muốn xem sản phẩm nổi bật")
            .build();
        
        when(productRecommendationService.getFeaturedProducts())
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("iPhone 15 Pro có camera tuyệt vời"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("FEATURED", response.getDetectedIntent());
        assertEquals(2, response.getRecommendedProducts().size());
        assertNotNull(response.getAiResponse());
        assertTrue(response.getAiResponse().contains("iPhone"));
        assertTrue(response.getProcessingTimeMs() > 0);
        
        // Kiểm tra product URL được thêm
        ChatbotAssistantUserResponse.RecommendedProductDTO firstProduct = 
            response.getRecommendedProducts().get(0);
        assertEquals("/products/1", firstProduct.getProductUrl());
        
        verify(productRecommendationService, times(1)).getFeaturedProducts();
    }

    /**
     * Test: Lấy sản phẩm bán chạy
     */
    @Test
    void testChatWithBestSellingIntent() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Sản phẩm bán chạy nhất là gì?")
            .build();
        
        when(productRecommendationService.getBestSellingProducts())
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("Những sản phẩm bán chạy"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("BEST_SELLING", response.getDetectedIntent());
        assertFalse(response.getRecommendedProducts().isEmpty());
        verify(productRecommendationService, times(1)).getBestSellingProducts();
    }

    /**
     * Test: Lấy sản phẩm mới nhất
     */
    @Test
    void testChatWithNewArrivalsIntent() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Sản phẩm mới nhất là gì?")
            .build();
        
        when(productRecommendationService.getNewArrivalsProducts())
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("Sản phẩm mới nhất"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("NEW_ARRIVALS", response.getDetectedIntent());
        verify(productRecommendationService, times(1)).getNewArrivalsProducts();
    }

    /**
     * Test: Tìm kiếm sản phẩm theo từ khóa
     */
    @Test
    void testChatWithSearchIntent() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Tôi muốn điện thoại với camera tốt")
            .build();
        
        when(productRecommendationService.searchProducts(
            anyString(), anyDouble(), anyDouble(), anyLong(), anyString()))
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("Camera tốt"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("SEARCH", response.getDetectedIntent());
        assertFalse(response.getRecommendedProducts().isEmpty());
        verify(productRecommendationService, times(1))
            .searchProducts(anyString(), anyDouble(), anyDouble(), anyLong(), anyString());
    }

    /**
     * Test: Lấy sản phẩm theo danh mục
     */
    @Test
    void testChatWithCategoryIntent() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Cho tôi xem sản phẩm danh mục smartphones")
            .categoryId(1L)
            .build();
        
        when(productRecommendationService.getProductsByCategory(1L))
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("Smartphones"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("CATEGORY", response.getDetectedIntent());
        verify(productRecommendationService, times(1)).getProductsByCategory(1L);
    }

    /**
     * Test: Xử lý request trống
     */
    @Test
    void testChatWithEmptyMessage() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("")
            .build();

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertEquals("SEARCH", response.getDetectedIntent());
        assertTrue(response.getRecommendedProducts().isEmpty());
    }

    /**
     * Test: Xử lý ngoại lệ từ Gemini
     */
    @Test
    void testChatHandlesGeminiException() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Sản phẩm nổi bật")
            .build();
        
        when(productRecommendationService.getFeaturedProducts())
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenThrow(new RuntimeException("Gemini API error"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAiResponse());
        // Nên trả về fallback response thay vì null
        assertTrue(response.getAiResponse().contains("nổi bật") || 
                  response.getAiResponse().contains("iPhone"));
    }

    /**
     * Test: Response có đầy đủ fields
     */
    @Test
    void testChatResponseComplete() {
        // Arrange
        ChatbotAssistantUserRequest request = ChatbotAssistantUserRequest.builder()
            .message("Sản phẩm nổi bật")
            .build();
        
        when(productRecommendationService.getFeaturedProducts())
            .thenReturn(mockProducts);
        when(fallbackService.executeWithFallback(anyString(), anyBoolean()))
            .thenReturn(createMockGeminiResponse("Response từ Gemini"));

        // Act
        ChatbotAssistantUserResponse response = chatbotService.chat(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAiResponse(), "aiResponse không được null");
        assertNotNull(response.getRecommendedProducts(), "recommendedProducts không được null");
        assertNotNull(response.getDetectedIntent(), "detectedIntent không được null");
        assertNotNull(response.getRelevanceScore(), "relevanceScore không được null");
        assertNotNull(response.getProcessingTimeMs(), "processingTimeMs không được null");
        
        // Kiểm tra product URL
        response.getRecommendedProducts().forEach(product -> {
            assertNotNull(product.getProductUrl(), "productUrl không được null");
            assertTrue(product.getProductUrl().startsWith("/products/"), 
                      "productUrl phải là /products/{id}");
        });
    }

    /**
     * Helper: Tạo mock response từ Gemini API
     */
    private String createMockGeminiResponse(String text) {
        return String.format("""
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "%s"
                      }
                    ]
                  }
                }
              ]
            }
            """, text);
    }
}
