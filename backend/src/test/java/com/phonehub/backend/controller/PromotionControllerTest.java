package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.phonehub.backend.dto.request.PromotionRequest;
import com.phonehub.backend.dto.response.PromotionResponse;
import com.phonehub.backend.enums.EPromotionStatus;
import com.phonehub.backend.service.intf.IPromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PromotionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPromotionService promotionService;

    @InjectMocks
    private PromotionController promotionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(promotionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void createPromotion_ShouldReturnCreatedPromotion() throws Exception {
        PromotionRequest request = new PromotionRequest();
        request.setTemplateId("temp_1");
        request.setTitle("Summer Sale");
        request.setEffectiveDate(LocalDateTime.now().plusDays(1));
        request.setExpirationDate(LocalDateTime.now().plusDays(10));
        request.setPercentDiscount(10.0);
        request.setStatus(EPromotionStatus.ACTIVE);

        PromotionResponse response = PromotionResponse.builder()
                .id("promo_1")
                .title("Summer Sale")
                .percentDiscount(10.0)
                .status(EPromotionStatus.ACTIVE)
                .build();

        when(promotionService.createPromotion(any(PromotionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Created successfully"))
                .andExpect(jsonPath("$.data.id").value("promo_1"));
    }

    @Test
    public void modifyPromotion_ShouldReturnUpdatedPromotion() throws Exception {
        String id = "promo_1";
        PromotionRequest request = new PromotionRequest();
        request.setTemplateId("temp_1");
        request.setTitle("Summer Sale V2");
        request.setEffectiveDate(LocalDateTime.now().plusDays(1));
        request.setExpirationDate(LocalDateTime.now().plusDays(10));
        request.setPercentDiscount(15.0);
        request.setStatus(EPromotionStatus.ACTIVE);

        PromotionResponse response = PromotionResponse.builder()
                .id(id)
                .title("Summer Sale V2")
                .percentDiscount(15.0)
                .status(EPromotionStatus.ACTIVE)
                .build();

        when(promotionService.modifyPromotion(eq(id), any(PromotionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/promotions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Modified successfully"))
                .andExpect(jsonPath("$.data.title").value("Summer Sale V2"));
    }

    @Test
    public void disablePromotion_ShouldReturnSuccess() throws Exception {
        String id = "promo_1";
        doNothing().when(promotionService).disable(id);

        mockMvc.perform(patch("/api/v1/admin/promotions/{id}/disable", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Disabled successfully"));
    }

    @Test
    public void getDetails_ShouldReturnPromotion() throws Exception {
        String id = "promo_1";
        PromotionResponse response = PromotionResponse.builder()
                .id(id)
                .title("Summer Sale")
                .percentDiscount(10.0)
                .status(EPromotionStatus.ACTIVE)
                .build();

        when(promotionService.getDetails(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/promotions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void getAllPromotions_ShouldReturnList() throws Exception {
        PromotionResponse response = PromotionResponse.builder()
                .id("promo_1")
                .title("Summer Sale")
                .build();

        when(promotionService.getAllPromotions()).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/admin/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllActivePromotions_ShouldReturnList() throws Exception {
        PromotionResponse response = PromotionResponse.builder()
                .id("promo_1")
                .title("Summer Sale")
                .build();

        when(promotionService.getAllActivePromotions()).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void checkAndGetAvailablePromotions_ShouldReturnList() throws Exception {
        PromotionResponse response = PromotionResponse.builder()
                .id("promo_1")
                .title("Summer Sale")
                .build();

        when(promotionService.checkAndGetAvailablePromotions(100.0)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/promotions/available").param("orderTotal", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void calculateDiscount_ShouldReturnDiscount() throws Exception {
        when(promotionService.calculateDiscount("promo_1", 100.0)).thenReturn(10.0);

        mockMvc.perform(get("/api/v1/promotions/calculate")
                .param("promotionId", "promo_1")
                .param("orderTotal", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10.0));
    }
}
