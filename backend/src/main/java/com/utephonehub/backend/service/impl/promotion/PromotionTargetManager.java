package com.utephonehub.backend.service.impl.promotion;

import com.utephonehub.backend.dto.request.PromotionRequest;
import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.PromotionTarget;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for Promotion Targets
 * Follows Single Responsibility Principle (SRP) - only handles target operations
 * Follows Creator (GRASP) - creates PromotionTarget instances
 */
@Component
public class PromotionTargetManager {

    /**
     * Save promotion targets from request
     * @param promotion Promotion entity
     * @param targetRequests List of target requests
     */
    public void saveTargets(Promotion promotion, List<PromotionRequest.TargetRequest> targetRequests) {
        if (targetRequests == null || targetRequests.isEmpty()) {
            return;
        }

        List<PromotionTarget> targets = createTargetsFromRequests(promotion, targetRequests);
        
        if (promotion.getTargets() == null) {
            promotion.setTargets(new ArrayList<>());
        }
        
        promotion.getTargets().addAll(targets);
    }

    /**
     * Clear and replace promotion targets
     * @param promotion Promotion entity
     * @param targetRequests New list of target requests
     */
    public void replaceTargets(Promotion promotion, List<PromotionRequest.TargetRequest> targetRequests) {
        if (promotion.getTargets() != null) {
            promotion.getTargets().clear();
        }
        saveTargets(promotion, targetRequests);
    }

    /**
     * Create PromotionTarget entities from requests
     * Follows Creator (GRASP) - responsible for creating PromotionTarget objects
     */
    private List<PromotionTarget> createTargetsFromRequests(
            Promotion promotion, 
            List<PromotionRequest.TargetRequest> targetRequests) {
        
        return targetRequests.stream()
                .map(request -> createTarget(promotion, request))
                .collect(Collectors.toList());
    }

    /**
     * Create single PromotionTarget from request
     */
    private PromotionTarget createTarget(Promotion promotion, PromotionRequest.TargetRequest request) {
        return PromotionTarget.builder()
                .applicableObjectId(request.getApplicableObjectId())
                .type(request.getType())
                .promotion(promotion)
                .build();
    }
}
