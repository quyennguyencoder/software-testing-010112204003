package com.utephonehub.backend.dto.response;

import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.enums.EPromotionTargetType;
import com.utephonehub.backend.enums.EPromotionTemplateType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromotionResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime effectiveDate;
    private LocalDateTime expirationDate;
    private Double percentDiscount;
    private Double fixedAmount;
    private Double maxDiscount;
    private Double minValueToBeApplied;
    private EPromotionStatus status;

    // Thông tin Template đi kèm (Flattening - Làm phẳng dữ liệu để FE dễ lấy)
    private String templateId;
    private String templateCode;
    private EPromotionTemplateType templateType;

    // Danh sách đối tượng áp dụng
    private List<TargetResponse> targets;

    @Data
    @Builder
    public static class TargetResponse {
        private Long id;
        private Long applicableObjectId;
        private EPromotionTargetType type;
    }
}