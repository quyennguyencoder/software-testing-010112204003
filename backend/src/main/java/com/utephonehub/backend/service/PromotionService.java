package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.PromotionRequest;
import com.utephonehub.backend.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {

    // --- ADMIN: MANAGE PROMOTION ---

    // SD: Create Promotion -> createPromotion()
    PromotionResponse createPromotion(PromotionRequest request);

    // SD: Modify Promotion -> modifyPromotion()
    PromotionResponse modifyPromotion(String id, PromotionRequest request);

    // SD: Disable Promotion -> disable()
    void disable(String id);

    // SD: See Promotion Detail -> getDetails()
    PromotionResponse getDetails(String id);

    // SD: Get All Promotions -> getAllPromotions()
    List<PromotionResponse> getAllPromotions();

    // --- CUSTOMER: APPLY PROMOTION ---

    // SD: Access Promotion List -> checkAndGetAvailablePromotions()
    // Hàm này lọc ra các khuyến mãi mà User có thể dùng được
    List<PromotionResponse> checkAndGetAvailablePromotions(Double orderTotal);

    // SD: Request apply Promotion -> calculateDiscount()
    Double calculateDiscount(String promotionId, Double orderTotal);
}