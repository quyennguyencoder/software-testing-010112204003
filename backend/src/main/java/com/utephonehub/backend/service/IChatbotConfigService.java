package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.response.ChatbotAssistantUserResponse;

/**
 * Interface Service quản lý cấu hình Chatbot
 * Cho phép Admin bật/tắt chatbot để quản lý chi phí và phòng tấn công
 */
public interface IChatbotConfigService {
    
    /**
     * Kiểm tra chatbot có được bật không
     * @return true nếu chatbot đang bật
     */
    boolean isChatbotEnabled();
    
    /**
     * Bật chatbot
     * @param adminEmail Email admin thực hiện
     * @return true nếu thành công
     */
    boolean enableChatbot(String adminEmail);
    
    /**
     * Tắt chatbot
     * @param adminEmail Email admin thực hiện
     * @return true nếu thành công
     */
    boolean disableChatbot(String adminEmail);
    
    /**
     * Toggle trạng thái chatbot
     * @param adminEmail Email admin thực hiện
     * @return trạng thái mới (true = bật, false = tắt)
     */
    boolean toggleChatbot(String adminEmail);
    
    /**
     * Lấy trạng thái chatbot hiện tại
     * @return ChatbotStatusResponse chứa trạng thái và thông tin
     */
    ChatbotStatusInfo getChatbotStatus();
    
    /**
     * Tạo phản hồi fallback khi chatbot bị tắt
     * Trả về câu chào chuyên nghiệp + sản phẩm nổi bật/mới/bán chạy
     * @return ChatbotAssistantUserResponse với fallback data
     */
    ChatbotAssistantUserResponse createFallbackResponse();
    
    /**
     * DTO nội bộ chứa thông tin trạng thái chatbot
     */
    record ChatbotStatusInfo(
        boolean enabled,
        String updatedBy,
        String updatedAt,
        String description
    ) {}
}
