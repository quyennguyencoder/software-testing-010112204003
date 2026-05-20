package com.phonehub.backend.service;

import com.phonehub.backend.dto.request.PromotionTemplateRequest;
import com.phonehub.backend.dto.response.PromotionTemplateResponse;

import java.util.List;

public interface IPromotionTemplateService {

    /**
     * Create new promotion template
     */
    PromotionTemplateResponse createTemplate(PromotionTemplateRequest request);

    /**
     * Update existing template
     */
    PromotionTemplateResponse updateTemplate(String id, PromotionTemplateRequest request);

    /**
     * Delete template
     */
    void deleteTemplate(String id);

    /**
     * Get template by ID
     */
    PromotionTemplateResponse getTemplateById(String id);

    /**
     * Get all templates
     */
    List<PromotionTemplateResponse> getAllTemplates();
}
