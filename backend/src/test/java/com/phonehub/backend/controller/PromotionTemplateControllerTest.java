package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.PromotionTemplateRequest;
import com.phonehub.backend.dto.response.PromotionTemplateResponse;
import com.phonehub.backend.enums.EPromotionTemplateType;
import com.phonehub.backend.service.intf.IPromotionTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PromotionTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPromotionTemplateService templateService;

    @InjectMocks
    private PromotionTemplateController templateController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(templateController).build();
    }

    @Test
    public void createTemplate_ShouldReturnCreatedTemplate() throws Exception {
        PromotionTemplateRequest request = new PromotionTemplateRequest();
        request.setCode("WELCOME10");
        request.setType(EPromotionTemplateType.DISCOUNT);

        PromotionTemplateResponse response = PromotionTemplateResponse.builder()
                .id("temp_1")
                .code("WELCOME10")
                .type(EPromotionTemplateType.DISCOUNT)
                .build();

        when(templateService.createTemplate(any(PromotionTemplateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/promotion-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template created successfully"));
    }

    @Test
    public void updateTemplate_ShouldReturnUpdatedTemplate() throws Exception {
        String id = "temp_1";
        PromotionTemplateRequest request = new PromotionTemplateRequest();
        request.setCode("WELCOME20");
        request.setType(EPromotionTemplateType.DISCOUNT);

        PromotionTemplateResponse response = PromotionTemplateResponse.builder()
                .id(id)
                .code("WELCOME20")
                .type(EPromotionTemplateType.DISCOUNT)
                .build();

        when(templateService.updateTemplate(eq(id), any(PromotionTemplateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/promotion-templates/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template updated successfully"));
    }

    @Test
    public void deleteTemplate_ShouldReturnSuccess() throws Exception {
        String id = "temp_1";
        doNothing().when(templateService).deleteTemplate(id);

        mockMvc.perform(delete("/api/v1/admin/promotion-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204)) // matching Response.noContent status
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getTemplateById_ShouldReturnTemplate() throws Exception {
        String id = "temp_1";
        PromotionTemplateResponse response = PromotionTemplateResponse.builder()
                .id(id)
                .code("WELCOME10")
                .type(EPromotionTemplateType.DISCOUNT)
                .build();

        when(templateService.getTemplateById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/promotion-templates/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void getAllTemplates_ShouldReturnTemplatesList() throws Exception {
        PromotionTemplateResponse response = PromotionTemplateResponse.builder()
                .id("temp_1")
                .code("WELCOME10")
                .type(EPromotionTemplateType.DISCOUNT)
                .build();

        when(templateService.getAllTemplates()).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/admin/promotion-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }
}