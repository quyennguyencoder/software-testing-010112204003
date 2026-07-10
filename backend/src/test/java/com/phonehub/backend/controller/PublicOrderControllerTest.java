package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.order.TrackOrderRequest;
import com.phonehub.backend.dto.response.order.PublicOrderTrackingResponse;
import com.phonehub.backend.service.intf.IPublicOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PublicOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPublicOrderService publicOrderService;

    @InjectMocks
    private PublicOrderController publicOrderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(publicOrderController).build();
    }

    @Test
    public void trackOrder_ShouldReturnTrackingResponse() throws Exception {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD-123");
        request.setEmail("customer@example.com");

        PublicOrderTrackingResponse response = new PublicOrderTrackingResponse();

        when(publicOrderService.trackOrder(any(TrackOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/public/orders/track")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void quickTrackOrder_ShouldReturnTrackingResponse() throws Exception {
        String code = "ORD-123";
        PublicOrderTrackingResponse response = new PublicOrderTrackingResponse();

        when(publicOrderService.quickTrackByCode(code)).thenReturn(response);

        mockMvc.perform(get("/api/v1/public/orders/quick-track/{orderCode}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void validateOrderAccess_ShouldReturnValidationMap() throws Exception {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD-123");
        request.setEmail("customer@example.com");

        when(publicOrderService.validateOrderAccess("ORD-123", "customer@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/v1/public/orders/validate-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasAccess").value(true));
    }

    @Test
    public void getTrackingGuide_ShouldReturnGuide() throws Exception {
        mockMvc.perform(get("/api/v1/public/orders/tracking-guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Hướng dẫn tra cứu đơn hàng"));
    }

    @Test
    public void getTrackingStatistics_ShouldReturnStats() throws Exception {
        when(publicOrderService.getTrackingStatistics()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/v1/public/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void validateOrderAccess_ShouldReturnFalseWhenNoAccess() throws Exception {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD-999");
        request.setEmail("noaccess@example.com");

        when(publicOrderService.validateOrderAccess("ORD-999", "noaccess@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/public/orders/validate-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasAccess").value(false))
                .andExpect(jsonPath("$.data.suggestion").isNotEmpty());
    }

    @Test
    public void validateOrderAccess_ServiceThrows_ShouldReturnServerError() throws Exception {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD-ERR");
        request.setEmail("err@example.com");

        when(publicOrderService.validateOrderAccess("ORD-ERR", "err@example.com")).thenThrow(new RuntimeException("boom"));
        assertThrows(Exception.class, () -> mockMvc.perform(post("/api/v1/public/orders/validate-access")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))));
    }
}
