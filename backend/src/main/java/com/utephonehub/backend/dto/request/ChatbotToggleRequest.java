package com.utephonehub.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để Admin bật/tắt chatbot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request bật/tắt chatbot")
public class ChatbotToggleRequest {
    
    @Schema(description = "Trạng thái mới của chatbot (true = bật, false = tắt)", example = "true")
    private Boolean enabled;
    
    @Schema(description = "Lý do thay đổi (tùy chọn)", example = "Tạm tắt để bảo trì hệ thống")
    private String reason;
}
