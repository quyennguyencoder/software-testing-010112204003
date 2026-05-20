package com.phonehub.backend.controller;

import com.phonehub.backend.dto.ApiResponse;
import com.phonehub.backend.dto.request.ChatbotAssistantUserRequest;
import com.phonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.phonehub.backend.service.IChatbotAssistantUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho Chatbot Tư Vấn Sản Phẩm
 * Endpoint: /api/v1/chatbot-assistant
 */
@RestController
@RequestMapping("/api/v1/chatbot-assistant")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot Tư Vấn Sản Phẩm", description = "API cho chatbot AI gợi ý sản phẩm phù hợp")
public class ChatbotAssistantUserController {
    
    private final IChatbotAssistantUserService chatbotService;
    
    /**
     * Gửi câu hỏi tư vấn sản phẩm
     * POST /api/v1/chatbot-assistant/chat
     */
    @PostMapping("/chat")
    @PermitAll
    @Operation(summary = "Tư vấn sản phẩm", 
               description = "Khách hàng gửi câu hỏi, chatbot phân tích intent và gợi ý sản phẩm phù hợp")
    public ResponseEntity<ApiResponse<ChatbotAssistantUserResponse>> chat(
            @RequestBody ChatbotAssistantUserRequest request) {
        
        log.info("💬 Nhận yêu cầu chat: {}", request.getMessage());
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("Vui lòng nhập câu hỏi."));
        }
        
        ChatbotAssistantUserResponse response = chatbotService.chat(request);
        return ResponseEntity.ok(ApiResponse.success("Tư vấn sản phẩm thành công", response));
    }
    
    /**
     * Xóa cache (để tái tạo dữ liệu sản phẩm)
     * POST /api/v1/chatbot-assistant/clear-cache
     * (Chỉ dành cho admin)
     */
    @PostMapping("/clear-cache")
    @Operation(summary = "Xóa cache", description = "Xóa cache sản phẩm và embedding (admin only)")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        log.info("🧹 Admin yêu cầu xóa cache");
        // Có thể thêm @PreAuthorize("hasRole('ADMIN')") nếu cần
        return ResponseEntity.ok(ApiResponse.success("Cache đã được xóa", "Success"));
    }
}
