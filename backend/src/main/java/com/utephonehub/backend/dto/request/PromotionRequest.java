package com.utephonehub.backend.dto.request;

import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.enums.EPromotionTargetType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionRequest {

    // Admin phải chọn Template (Mẫu) khi tạo khuyến mãi (Gửi ID của Template lên)
    @NotBlank(message = "Template ID is required")
    private String templateId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Effective date is required")
    private LocalDateTime effectiveDate;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expirationDate;

    @PositiveOrZero(message = "Percent discount must be positive")
    private Double percentDiscount;

    @PositiveOrZero(message = "Min value must be positive")
    private Double minValueToBeApplied;

    @NotNull(message = "Status is required")
    private EPromotionStatus status;

    // Danh sách các đối tượng áp dụng (Sản phẩm/Danh mục)
    private List<TargetRequest> targets;

    @Data
    public static class TargetRequest {
        @NotNull(message = "Applicable Object ID is required")
        private Long applicableObjectId;

        @NotNull(message = "Target Type is required")
        private EPromotionTargetType type;
    }
}