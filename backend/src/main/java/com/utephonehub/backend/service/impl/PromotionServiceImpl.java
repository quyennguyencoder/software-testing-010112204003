package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.PromotionRequest;
import com.utephonehub.backend.dto.response.PromotionResponse;
import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.PromotionTarget;
import com.utephonehub.backend.entity.PromotionTemplate;
import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.enums.EPromotionTemplateType;
import com.utephonehub.backend.exception.promotion.PromotionNotFoundException;
import com.utephonehub.backend.mapper.PromotionMapper;
import com.utephonehub.backend.repository.PromotionRepository;
import com.utephonehub.backend.repository.PromotionTemplateRepository;
import com.utephonehub.backend.service.IPromotionService;
import com.utephonehub.backend.service.impl.promotion.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IPromotionService
 * Refactored following SOLID, DRY, and GRASP principles:
 * - Single Responsibility: Delegates specific tasks to helper classes
 * - Open/Closed: Open for extension through new validators/calculators
 * - Dependency Inversion: Depends on abstractions (helper components)
 * - DRY: No code duplication, reusable components
 * - High Cohesion: Each component has a clear, focused purpose
 * - Low Coupling: Components are independent and loosely coupled
 */
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements IPromotionService {

    // Repositories
    private final PromotionRepository promotionRepository;
    private final PromotionTemplateRepository templateRepository;

    // Helper Components (following Dependency Injection and Indirection patterns)
    private final PromotionValidator promotionValidator;
    private final PromotionDiscountCalculator discountCalculator;
    private final PromotionMapper promotionMapper;
    private final PromotionTargetManager targetManager;

    // --- 1. CREATE PROMOTION ---
    @Override
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        PromotionTemplate template = findTemplateOrThrow(request.getTemplateId());

        Promotion promotion = buildPromotionFromRequest(request, template);
        Promotion savedPromotion = promotionRepository.save(promotion);
        
        targetManager.saveTargets(savedPromotion, request.getTargets());

        return promotionMapper.toResponse(savedPromotion);
    }

    // --- 2. MODIFY PROMOTION ---
    @Override
    @Transactional
    public PromotionResponse modifyPromotion(String id, PromotionRequest request) {
        Promotion promotion = findPromotionOrThrow(id);

        updatePromotionFields(promotion, request);
        updatePromotionTemplate(promotion, request.getTemplateId());
        targetManager.replaceTargets(promotion, request.getTargets());

        Promotion updatedPromotion = promotionRepository.save(promotion);
        return promotionMapper.toResponse(updatedPromotion);
    }

    // --- 3. DISABLE PROMOTION ---
    @Override
    @Transactional
    public void disable(String id) {
        Promotion promotion = findPromotionOrThrow(id);
        promotion.setStatus(EPromotionStatus.INACTIVE);
        promotionRepository.save(promotion);
    }

    // --- 4. GET DETAILS ---
    @Override
    public PromotionResponse getDetails(String id) {
        Promotion promotion = findPromotionOrThrow(id);
        return promotionMapper.toResponse(promotion);
    }

    // --- 5. GET ALL PROMOTIONS ---
    @Override
    public List<PromotionResponse> getAllPromotions() {
        // Auto-update expired promotions status
        updateExpiredPromotionsStatus();
        
        List<Promotion> promotions = promotionRepository.findAll();
        return promotionMapper.toResponseList(promotions);
    }

    // --- PUBLIC: GET ALL ACTIVE ---
    @Override
    public List<PromotionResponse> getAllActivePromotions() {
        // Auto-update expired promotions status
        updateExpiredPromotionsStatus();
        
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(now, now);
        
        return promotions.stream()
                .filter(p -> p.getStatus() == EPromotionStatus.ACTIVE)
                // Filter out DISCOUNT promotions - they auto-apply to products, not for user selection
                .filter(p -> p.getTemplate().getType() != EPromotionTemplateType.DISCOUNT)
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- 6. CHECK AVAILABLE ---
    @Override
    public List<PromotionResponse> checkAndGetAvailablePromotions(Double orderTotal) {
        // Auto-update expired promotions status
        updateExpiredPromotionsStatus();
        
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findByEffectiveDateBeforeAndExpirationDateAfter(now, now);

        return promotions.stream()
                .filter(p -> p.getStatus() == EPromotionStatus.ACTIVE)
                // Filter out DISCOUNT promotions - they auto-apply to products, not for user selection
                .filter(p -> p.getTemplate().getType() != EPromotionTemplateType.DISCOUNT)
                .filter(p -> isMinValueMet(p, orderTotal))
                .map(promotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // --- 7. CALCULATE DISCOUNT ---
    @Override
    public Double calculateDiscount(String promotionId, Double orderTotal) {
        Promotion promotion = findPromotionOrThrow(promotionId);
        
        // Validate promotion can be applied (following Single Responsibility)
        promotionValidator.validatePromotionApplicability(promotion, orderTotal);
        
        // Calculate discount (following Single Responsibility)
        return discountCalculator.calculateDiscountAmount(promotion, orderTotal);
    }

    // --- PRIVATE HELPER METHODS ---
    
    /**
     * Find promotion by ID or throw exception
     * Follows DRY principle - centralized error handling
     */
    private Promotion findPromotionOrThrow(String id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    /**
     * Find template by ID or throw exception
     * Follows DRY principle - centralized error handling
     */
    private PromotionTemplate findTemplateOrThrow(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
    }

    /**
     * Build Promotion entity from request
     * Follows Creator (GRASP) pattern
     */
    private Promotion buildPromotionFromRequest(PromotionRequest request, PromotionTemplate template) {
        return Promotion.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .effectiveDate(request.getEffectiveDate())
                .expirationDate(request.getExpirationDate())
                .percentDiscount(request.getPercentDiscount())
                .minValueToBeApplied(request.getMinValueToBeApplied())
                .status(request.getStatus())
                .template(template)
                .targets(new ArrayList<>())
                .build();
    }

    /**
     * Update promotion fields from request
     * Follows Information Expert (GRASP) - service knows how to update promotion
     */
    private void updatePromotionFields(Promotion promotion, PromotionRequest request) {
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setEffectiveDate(request.getEffectiveDate());
        promotion.setExpirationDate(request.getExpirationDate());
        promotion.setPercentDiscount(request.getPercentDiscount());
        promotion.setMinValueToBeApplied(request.getMinValueToBeApplied());
        promotion.setStatus(request.getStatus()); // Update status from request
    }

    /**
     * Update promotion template if changed
     * Follows Single Responsibility - focused method for template update
     */
    private void updatePromotionTemplate(Promotion promotion, String newTemplateId) {
        if (!promotion.getTemplate().getId().equals(newTemplateId)) {
            PromotionTemplate newTemplate = findTemplateOrThrow(newTemplateId);
            promotion.setTemplate(newTemplate);
        }
    }

    /**
     * Check if order total meets minimum value requirement
     * Follows DRY principle - reusable validation logic
     */
    private boolean isMinValueMet(Promotion promotion, Double orderTotal) {
        return promotion.getMinValueToBeApplied() == null 
                || orderTotal >= promotion.getMinValueToBeApplied();
    }

    /**
     * Auto-update expired promotions status
     * Changes status from ACTIVE to INACTIVE for expired promotions
     * Does NOT change the promotion title - only updates status
     * Follows Single Responsibility - focused on status management
     */
    @Transactional
    private void updateExpiredPromotionsStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find all ACTIVE promotions that have expired
        List<Promotion> expiredPromotions = promotionRepository.findAll().stream()
                .filter(p -> p.getStatus() == EPromotionStatus.ACTIVE)
                .filter(p -> now.isAfter(p.getExpirationDate()))
                .collect(Collectors.toList());
        
        // Update status to INACTIVE (keep original title)
        if (!expiredPromotions.isEmpty()) {
            expiredPromotions.forEach(promotion -> {
                promotion.setStatus(EPromotionStatus.INACTIVE);
            });
            promotionRepository.saveAll(expiredPromotions);
        }
    }

    /**
     * Get the best active DISCOUNT promotion for a product
     * Automatically applies to products based on PRODUCT/CATEGORY/BRAND targets
     * Returns highest discount percentage among applicable promotions
     * 
     * @param productId Product ID
     * @param categoryId Category ID of the product
     * @param brandId Brand ID of the product
     * @return Best discount percentage (0-100), or null if no discount
     */
    @Override
    public Double getBestDiscountForProduct(Long productId, Long categoryId, Long brandId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all active DISCOUNT promotions
        List<Promotion> activeDiscounts = promotionRepository.findAll().stream()
                .filter(p -> p.getStatus() == EPromotionStatus.ACTIVE)
                .filter(p -> p.getTemplate().getType() == EPromotionTemplateType.DISCOUNT)
                .filter(p -> now.isAfter(p.getEffectiveDate()) && now.isBefore(p.getExpirationDate()))
                .collect(Collectors.toList());
        
        // Find applicable promotions and collect their discount percentages
        Double maxDiscount = activeDiscounts.stream()
                .filter(promotion -> isPromotionApplicableToProduct(promotion, productId, categoryId, brandId))
                .map(Promotion::getPercentDiscount)
                .filter(discount -> discount != null && discount > 0)
                .max(Double::compare)
                .orElse(null);
        
        return maxDiscount;
    }

    /**
     * Check if a promotion is applicable to a specific product
     * Checks if promotion targets include the product's ID, category, or brand
     */
    private boolean isPromotionApplicableToProduct(Promotion promotion, Long productId, Long categoryId, Long brandId) {
        if (promotion.getTargets() == null || promotion.getTargets().isEmpty()) {
            return false;
        }
        
        for (PromotionTarget target : promotion.getTargets()) {
            Long targetId = target.getApplicableObjectId();
            
            switch (target.getType()) {
                case PRODUCT:
                    if (productId != null && productId.equals(targetId)) {
                        return true;
                    }
                    break;
                case CATEGORY:
                    if (categoryId != null && categoryId.equals(targetId)) {
                        return true;
                    }
                    break;
                case BRAND:
                    if (brandId != null && brandId.equals(targetId)) {
                        return true;
                    }
                    break;
            }
        }
        
        return false;
    }
}