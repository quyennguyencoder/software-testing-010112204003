package com.utephonehub.backend.dto.response;

import com.utephonehub.backend.enums.EPromotionTemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionTemplateResponse {

    private String id;
    private String code;
    private EPromotionTemplateType type;
    private LocalDateTime createdAt;
}
