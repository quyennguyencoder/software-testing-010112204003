package com.utephonehub.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO chứa trạng thái chatbot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trạng thái chatbot")
public class ChatbotStatusResponse {
    
    @Schema(description = "Chatbot đang bật hay tắt", example = "true")
    private boolean enabled;
    
    @Schema(description = "Trạng thái hiển thị", example = "BẬT")
    private String status;
    
    @Schema(description = "Admin đã cập nhật lần cuối", example = "admin@ute.edu.vn")
    private String updatedBy;
    
    @Schema(description = "Thời gian cập nhật lần cuối", example = "01/01/2026 10:30:00")
    private String updatedAt;
    
    @Schema(description = "Mô tả", example = "Chatbot đang hoạt động bình thường")
    private String description;
    
    @Schema(description = "Thông báo cho admin", example = "Chatbot đã được BẬT thành công")
    private String message;
}
