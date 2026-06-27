package com.phonehub.backend.service.impl.promotion;

import com.phonehub.backend.dto.request.PromotionRequest;
import com.phonehub.backend.entity.Promotion;
import com.phonehub.backend.entity.PromotionTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class PromotionTargetManagerTest {

    @InjectMocks
    private PromotionTargetManager manager;

    private Promotion promotion;
    private List<PromotionRequest.TargetRequest> targetRequests;

    @BeforeEach
    void setUp() {
        promotion = new Promotion();
        promotion.setTargets(new ArrayList<>());

        targetRequests = new ArrayList<>();
        PromotionRequest.TargetRequest request = new PromotionRequest.TargetRequest();
        request.setApplicableObjectId(1L);
        targetRequests.add(request);
    }

    @Test
    void saveTargets_ValidRequests_AddsTargets() {
        manager.saveTargets(promotion, targetRequests);

        assertNotNull(promotion.getTargets());
        assertEquals(1, promotion.getTargets().size());
        assertEquals(1L, promotion.getTargets().get(0).getApplicableObjectId());
    }

    @Test
    void saveTargets_NullRequests_DoesNothing() {
        manager.saveTargets(promotion, null);

        assertEquals(0, promotion.getTargets().size());
    }

    @Test
    void saveTargets_EmptyRequests_DoesNothing() {
        manager.saveTargets(promotion, Collections.emptyList());

        assertEquals(0, promotion.getTargets().size());
    }

    @Test
    void saveTargets_NullTargetsListInPromotion_InitializesAndAdds() {
        promotion.setTargets(null);
        manager.saveTargets(promotion, targetRequests);

        assertNotNull(promotion.getTargets());
        assertEquals(1, promotion.getTargets().size());
    }

    @Test
    void replaceTargets_ValidRequests_ReplacesTargets() {
        PromotionTarget oldTarget = new PromotionTarget();
        oldTarget.setApplicableObjectId(99L);
        promotion.getTargets().add(oldTarget);

        manager.replaceTargets(promotion, targetRequests);

        assertEquals(1, promotion.getTargets().size());
        assertEquals(1L, promotion.getTargets().get(0).getApplicableObjectId());
    }

    @Test
    void replaceTargets_NullTargetsListInPromotion_InitializesAndAdds() {
        promotion.setTargets(null);

        manager.replaceTargets(promotion, targetRequests);

        assertNotNull(promotion.getTargets());
        assertEquals(1, promotion.getTargets().size());
    }
}