package com.utephonehub.backend.dto.request;

import com.utephonehub.backend.enums.EPromotionTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionTemplateRequest {

    @NotBlank(message = "Template code is required")
    private String code;

    @NotNull(message = "Template type is required")
    private EPromotionTemplateType type;
}
