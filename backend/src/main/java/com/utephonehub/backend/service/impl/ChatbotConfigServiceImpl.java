package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.utephonehub.backend.dto.response.ChatbotAssistantUserResponse.RecommendedProductDTO;
import com.utephonehub.backend.dto.response.productview.ProductCardResponse;
import com.utephonehub.backend.entity.ChatbotConfig;
import com.utephonehub.backend.repository.ChatbotConfigRepository;
import com.utephonehub.backend.service.IChatbotConfigService;
import com.utephonehub.backend.service.IProductViewService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Implementation của IChatbotConfigService
 * Quản lý trạng thái bật/tắt chatbot và tạo fallback response
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotConfigServiceImpl implements IChatbotConfigService {

    private final ChatbotConfigRepository configRepository;
    private final IProductViewService productViewService;
    
    // Danh sách 10 câu chào hỏi và tư vấn chuyên nghiệp
    private static final List<String> FALLBACK_GREETINGS = List.of(
        "Chào bạn! 👋 Cảm ơn bạn đã ghé thăm UTE Phone Hub. Dưới đây là những sản phẩm nổi bật, mới nhất và bán chạy nhất mà bạn có thể quan tâm!",
        "Xin chào! 🎉 Rất vui được hỗ trợ bạn tại UTE Phone Hub. Hãy khám phá các sản phẩm tuyệt vời bên dưới nhé!",
        "Chào mừng bạn đến với UTE Phone Hub! 📱 Chúng tôi có những smartphone chất lượng nhất dành cho bạn.",
        "Xin chào quý khách! 💼 UTE Phone Hub tự hào mang đến những sản phẩm điện thoại chính hãng với giá tốt nhất.",
        "Chào bạn! 🌟 Bạn đang tìm kiếm điện thoại phù hợp? Hãy xem qua các gợi ý từ chúng tôi nhé!",
        "Hello! 👋 Chào mừng bạn đến UTE Phone Hub - nơi bạn tìm thấy smartphone hoàn hảo cho mình!",
        "Xin chào! 📲 Cảm ơn bạn đã tin tưởng UTE Phone Hub. Dưới đây là những sản phẩm đáng chú ý nhất!",
        "Chào bạn! 🎊 UTE Phone Hub luôn sẵn sàng tư vấn cho bạn. Hãy khám phá các sản phẩm hot nhất ngay!",
        "Xin chào quý khách! ⭐ Bạn đang tìm điện thoại mới? Chúng tôi có những lựa chọn tuyệt vời dành cho bạn!",
        "Chào mừng! 🙌 UTE Phone Hub - Đồng hành cùng bạn trong mọi kết nối. Khám phá ngay những sản phẩm nổi bật!"
    );
    
    // Câu kết thúc tư vấn
    private static final List<String> FALLBACK_CLOSINGS = List.of(
        "💡 Bạn có thể xem chi tiết sản phẩm bằng cách nhấp vào hình ảnh. Nếu cần tư vấn thêm, hãy liên hệ hotline: 1900-xxxx",
        "📞 Để được tư vấn chi tiết hơn, vui lòng liên hệ hotline: 1900-xxxx hoặc inbox trực tiếp cho chúng tôi.",
        "🛒 Đặt hàng ngay hôm nay để nhận ưu đãi đặc biệt! Hotline hỗ trợ: 1900-xxxx",
        "✨ Cam kết chính hãng 100%, bảo hành toàn quốc. Liên hệ ngay: 1900-xxxx",
        "🎁 Mua ngay để nhận quà tặng hấp dẫn! Tư vấn 24/7: 1900-xxxx"
    );

    /**
     * Khởi tạo cấu hình mặc định nếu chưa có
     */
    @PostConstruct
    @Transactional
    public void initDefaultConfig() {
        if (!configRepository.existsByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)) {
            ChatbotConfig config = ChatbotConfig.builder()
                .configKey(ChatbotConfig.KEY_CHATBOT_ENABLED)
                .configValue("true")
                .description("Bật/tắt tính năng chatbot AI. Khi tắt, hệ thống sẽ trả về các sản phẩm nổi bật/mới/bán chạy thay vì gọi AI.")
                .updatedBy("SYSTEM")
                .build();
            configRepository.save(config);
            log.info("✅ Đã tạo cấu hình mặc định cho CHATBOT_ENABLED = true");
        }
    }

    @Override
    public boolean isChatbotEnabled() {
        return configRepository.findByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)
            .map(config -> "true".equalsIgnoreCase(config.getConfigValue()))
            .orElse(true); // Mặc định là bật
    }

    @Override
    @Transactional
    public boolean enableChatbot(String adminEmail) {
        return updateChatbotStatus(true, adminEmail);
    }

    @Override
    @Transactional
    public boolean disableChatbot(String adminEmail) {
        return updateChatbotStatus(false, adminEmail);
    }

    @Override
    @Transactional
    public boolean toggleChatbot(String adminEmail) {
        boolean currentStatus = isChatbotEnabled();
        boolean newStatus = !currentStatus;
        updateChatbotStatus(newStatus, adminEmail);
        log.info("🔄 Admin {} đã {} chatbot", adminEmail, newStatus ? "BẬT" : "TẮT");
        return newStatus;
    }

    @Override
    public ChatbotStatusInfo getChatbotStatus() {
        return configRepository.findByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)
            .map(config -> new ChatbotStatusInfo(
                "true".equalsIgnoreCase(config.getConfigValue()),
                config.getUpdatedBy(),
                config.getUpdatedAt() != null 
                    ? config.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    : null,
                config.getDescription()
            ))
            .orElse(new ChatbotStatusInfo(true, "SYSTEM", null, "Cấu hình mặc định"));
    }

    @Override
    public ChatbotAssistantUserResponse createFallbackResponse() {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🤖 Chatbot đang TẮT - Tạo fallback response với sản phẩm nổi bật/mới/bán chạy");
            
            // Lấy sản phẩm nổi bật (featured)
            List<ProductCardResponse> featuredProducts = productViewService.getFeaturedProducts(4);
            
            // Lấy sản phẩm mới nhất
            List<ProductCardResponse> newArrivals = productViewService.getNewArrivals(4);
            
            // Lấy sản phẩm bán chạy
            List<ProductCardResponse> bestSelling = productViewService.getBestSellingProducts(4);
            
            // Gộp và loại bỏ trùng lặp, tối đa 10 sản phẩm
            List<RecommendedProductDTO> recommendedProducts = mergeAndConvertProducts(
                featuredProducts, newArrivals, bestSelling
            );
            
            // Chọn ngẫu nhiên câu chào và câu kết
            String greeting = getRandomGreeting();
            String closing = getRandomClosing();
            
            // Tạo AI response text
            String aiResponse = buildFallbackAiResponse(
                greeting, closing, 
                featuredProducts.size(), 
                newArrivals.size(), 
                bestSelling.size()
            );
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            return ChatbotAssistantUserResponse.builder()
                .aiResponse(aiResponse)
                .recommendedProducts(recommendedProducts)
                .detectedIntent("FALLBACK_MODE")
                .relevanceScore(1.0)
                .processingTimeMs(processingTime)
                .build();
                
        } catch (Exception e) {
            log.error("❌ Lỗi tạo fallback response: {}", e.getMessage(), e);
            return createErrorFallbackResponse(startTime);
        }
    }

    // ==================== PRIVATE METHODS ====================

    private boolean updateChatbotStatus(boolean enabled, String adminEmail) {
        ChatbotConfig config = configRepository.findByConfigKey(ChatbotConfig.KEY_CHATBOT_ENABLED)
            .orElseGet(() -> ChatbotConfig.builder()
                .configKey(ChatbotConfig.KEY_CHATBOT_ENABLED)
                .description("Bật/tắt tính năng chatbot AI")
                .build());
        
        config.setConfigValue(enabled ? "true" : "false");
        config.setUpdatedBy(adminEmail);
        configRepository.save(config);
        
        log.info("✅ Đã cập nhật CHATBOT_ENABLED = {} bởi {}", enabled, adminEmail);
        return true;
    }

    private String getRandomGreeting() {
        int index = ThreadLocalRandom.current().nextInt(FALLBACK_GREETINGS.size());
        return FALLBACK_GREETINGS.get(index);
    }

    private String getRandomClosing() {
        int index = ThreadLocalRandom.current().nextInt(FALLBACK_CLOSINGS.size());
        return FALLBACK_CLOSINGS.get(index);
    }

    private String buildFallbackAiResponse(String greeting, String closing, 
            int featuredCount, int newCount, int bestSellingCount) {
        
        StringBuilder sb = new StringBuilder();
        sb.append(greeting).append("\n\n");
        
        sb.append("📌 **Sản phẩm nổi bật**: ").append(featuredCount).append(" sản phẩm\n");
        sb.append("🆕 **Sản phẩm mới nhất**: ").append(newCount).append(" sản phẩm\n");
        sb.append("🔥 **Sản phẩm bán chạy**: ").append(bestSellingCount).append(" sản phẩm\n\n");
        
        sb.append(closing);
        
        return sb.toString();
    }

    /**
     * Gộp 3 danh sách sản phẩm, loại bỏ trùng lặp theo ID
     * Mỗi loại sẽ có reason khác nhau
     */
    private List<RecommendedProductDTO> mergeAndConvertProducts(
            List<ProductCardResponse> featured,
            List<ProductCardResponse> newArrivals,
            List<ProductCardResponse> bestSelling) {
        
        Map<Long, RecommendedProductDTO> productMap = new LinkedHashMap<>();
        
        // Thêm featured products với reason
        for (ProductCardResponse p : featured) {
            if (!productMap.containsKey(p.getId())) {
                productMap.put(p.getId(), convertToRecommendedDTO(p, "🌟 Sản phẩm nổi bật"));
            }
        }
        
        // Thêm new arrivals
        for (ProductCardResponse p : newArrivals) {
            if (!productMap.containsKey(p.getId())) {
                productMap.put(p.getId(), convertToRecommendedDTO(p, "🆕 Sản phẩm mới"));
            }
        }
        
        // Thêm best selling
        for (ProductCardResponse p : bestSelling) {
            if (!productMap.containsKey(p.getId())) {
                productMap.put(p.getId(), convertToRecommendedDTO(p, "🔥 Bán chạy nhất"));
            }
        }
        
        // Giới hạn 10 sản phẩm
        return productMap.values().stream()
            .limit(10)
            .collect(Collectors.toList());
    }

    private RecommendedProductDTO convertToRecommendedDTO(ProductCardResponse product, String reason) {
        // Xác định giá hiển thị (ưu tiên giá khuyến mãi nếu có)
        Double displayPrice = null;
        if (product.getDiscountedPrice() != null) {
            displayPrice = product.getDiscountedPrice().doubleValue();
        } else if (product.getMinPrice() != null) {
            displayPrice = product.getMinPrice().doubleValue();
        }
        
        // Xác định discountPercent từ discountPercentage
        Integer discountPct = product.getDiscountPercentage() != null 
            ? product.getDiscountPercentage().intValue() 
            : null;
        
        return RecommendedProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(null) // ProductCardResponse không có description
            .price(displayPrice)
            .originalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().doubleValue() : null)
            .rating(product.getAverageRating())
            .reviewCount(product.getTotalReviews())
            .imageUrl(product.getThumbnailUrl())
            .categoryName(product.getCategoryName())
            .matchScore(1.0)
            .reason(reason)
            .productUrl("/products/" + product.getId())
            .ram(product.getRam())
            .storage(product.getStorage())
            .batteryCapacity(product.getBatteryCapacity())
            .operatingSystem(product.getOperatingSystem())
            .brandName(product.getBrandName())
            .discountPercent(discountPct)
            .hasDiscount(product.getHasDiscount() != null && product.getHasDiscount())
            .build();
    }

    private ChatbotAssistantUserResponse createErrorFallbackResponse(long startTime) {
        return ChatbotAssistantUserResponse.builder()
            .aiResponse("Xin chào! 👋 Cảm ơn bạn đã ghé thăm UTE Phone Hub. " +
                "Hiện tại hệ thống đang bận, vui lòng thử lại sau hoặc liên hệ hotline: 1900-xxxx")
            .recommendedProducts(Collections.emptyList())
            .detectedIntent("FALLBACK_ERROR")
            .relevanceScore(0.0)
            .processingTimeMs(System.currentTimeMillis() - startTime)
            .build();
    }
}
