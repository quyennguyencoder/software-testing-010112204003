package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.ChatbotToggleRequest;
import com.utephonehub.backend.dto.response.ChatbotStatusResponse;
import com.utephonehub.backend.service.IChatbotConfigService;
import com.utephonehub.backend.service.IChatbotConfigService.ChatbotStatusInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Admin quản lý cấu hình Chatbot
 * Endpoint: /api/v1/admin/chatbot
 * 
 * Chức năng:
 * - Bật/tắt chatbot AI (phòng tấn công tốn request)
 * - Xem trạng thái chatbot
 * - Toggle nhanh trạng thái
 */
@RestController
@RequestMapping("/api/v1/admin/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Quản lý Chatbot", description = "API Admin quản lý bật/tắt chatbot AI")
@PreAuthorize("hasRole('ADMIN')")
public class ChatbotAdminController {

    private final IChatbotConfigService chatbotConfigService;

    /**
     * Lấy trạng thái chatbot hiện tại
     * GET /api/v1/admin/chatbot/status
     */
    @GetMapping("/status")
    @Operation(summary = "Xem trạng thái chatbot", 
               description = "Lấy thông tin trạng thái bật/tắt của chatbot AI")
    public ResponseEntity<ApiResponse<ChatbotStatusResponse>> getStatus() {
        log.info("📊 Admin yêu cầu xem trạng thái chatbot");
        
        ChatbotStatusInfo info = chatbotConfigService.getChatbotStatus();
        
        ChatbotStatusResponse response = ChatbotStatusResponse.builder()
            .enabled(info.enabled())
            .status(info.enabled() ? "BẬT" : "TẮT")
            .updatedBy(info.updatedBy())
            .updatedAt(info.updatedAt())
            .description(info.description())
            .message("Trạng thái hiện tại: " + (info.enabled() ? "Chatbot đang BẬT" : "Chatbot đang TẮT"))
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái chatbot thành công", response));
    }

    /**
     * Bật chatbot
     * POST /api/v1/admin/chatbot/enable
     */
    @PostMapping("/enable")
    @Operation(summary = "Bật chatbot", 
               description = "Bật tính năng chatbot AI để phản hồi câu hỏi")
    public ResponseEntity<ApiResponse<ChatbotStatusResponse>> enableChatbot(
            @AuthenticationPrincipal Jwt jwt) {
        
        String adminEmail = getAdminEmail(jwt);
        log.info("🟢 Admin {} yêu cầu BẬT chatbot", adminEmail);
        
        chatbotConfigService.enableChatbot(adminEmail);
        
        ChatbotStatusResponse response = ChatbotStatusResponse.builder()
            .enabled(true)
            .status("BẬT")
            .updatedBy(adminEmail)
            .message("✅ Chatbot đã được BẬT thành công! AI sẽ phản hồi câu hỏi của khách hàng.")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Đã bật chatbot thành công", response));
    }

    /**
     * Tắt chatbot
     * POST /api/v1/admin/chatbot/disable
     */
    @PostMapping("/disable")
    @Operation(summary = "Tắt chatbot", 
               description = "Tắt tính năng chatbot AI (phòng tấn công/tốn request). " +
                           "Khi tắt, hệ thống sẽ trả về sản phẩm nổi bật/mới/bán chạy thay thế.")
    public ResponseEntity<ApiResponse<ChatbotStatusResponse>> disableChatbot(
            @AuthenticationPrincipal Jwt jwt) {
        
        String adminEmail = getAdminEmail(jwt);
        log.info("🔴 Admin {} yêu cầu TẮT chatbot", adminEmail);
        
        chatbotConfigService.disableChatbot(adminEmail);
        
        ChatbotStatusResponse response = ChatbotStatusResponse.builder()
            .enabled(false)
            .status("TẮT")
            .updatedBy(adminEmail)
            .message("⚠️ Chatbot đã được TẮT! Hệ thống sẽ trả về sản phẩm nổi bật/mới/bán chạy thay thế.")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Đã tắt chatbot thành công", response));
    }

    /**
     * Toggle trạng thái chatbot
     * POST /api/v1/admin/chatbot/toggle
     */
    @PostMapping("/toggle")
    @Operation(summary = "Toggle chatbot", 
               description = "Đảo trạng thái chatbot (BẬT <-> TẮT)")
    public ResponseEntity<ApiResponse<ChatbotStatusResponse>> toggleChatbot(
            @AuthenticationPrincipal Jwt jwt) {
        
        String adminEmail = getAdminEmail(jwt);
        log.info("🔄 Admin {} yêu cầu TOGGLE chatbot", adminEmail);
        
        boolean newStatus = chatbotConfigService.toggleChatbot(adminEmail);
        
        ChatbotStatusResponse response = ChatbotStatusResponse.builder()
            .enabled(newStatus)
            .status(newStatus ? "BẬT" : "TẮT")
            .updatedBy(adminEmail)
            .message(newStatus 
                ? "✅ Chatbot đã được BẬT! AI sẽ phản hồi câu hỏi." 
                : "⚠️ Chatbot đã được TẮT! Sẽ hiển thị sản phẩm thay thế.")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(
            "Đã " + (newStatus ? "bật" : "tắt") + " chatbot thành công", response));
    }

    /**
     * Cập nhật trạng thái chatbot theo request body
     * PUT /api/v1/admin/chatbot/config
     */
    @PutMapping("/config")
    @Operation(summary = "Cập nhật cấu hình chatbot", 
               description = "Cập nhật trạng thái chatbot theo request body")
    public ResponseEntity<ApiResponse<ChatbotStatusResponse>> updateConfig(
            @RequestBody ChatbotToggleRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        String adminEmail = getAdminEmail(jwt);
        Boolean enabled = request.getEnabled();
        
        if (enabled == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Vui lòng cung cấp trạng thái enabled (true/false)"));
        }
        
        log.info("⚙️ Admin {} cập nhật cấu hình chatbot: enabled={}, reason={}", 
            adminEmail, enabled, request.getReason());
        
        if (enabled) {
            chatbotConfigService.enableChatbot(adminEmail);
        } else {
            chatbotConfigService.disableChatbot(adminEmail);
        }
        
        ChatbotStatusResponse response = ChatbotStatusResponse.builder()
            .enabled(enabled)
            .status(enabled ? "BẬT" : "TẮT")
            .updatedBy(adminEmail)
            .description(request.getReason())
            .message(enabled 
                ? "✅ Chatbot đã được BẬT thành công!" 
                : "⚠️ Chatbot đã được TẮT thành công!")
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình thành công", response));
    }

    // ==================== PRIVATE METHODS ====================

    private String getAdminEmail(Jwt jwt) {
        if (jwt == null) {
            return "UNKNOWN_ADMIN";
        }
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("sub");
        }
        return email != null ? email : "UNKNOWN_ADMIN";
    }
}
