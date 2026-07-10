package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.guestcart.GuestCartUpdateRequest;
import com.phonehub.backend.service.intf.IGuestCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GuestCartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IGuestCartService guestCartService;

    @InjectMocks
    private GuestCartController guestCartController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(guestCartController).build();
    }

    @Test
    public void createGuestCart_ShouldReturnSessionResponse() throws Exception {
        String mockGuestCartId = "guest_123456";

        when(guestCartService.allowCreateGuestCart(any(), anyInt())).thenReturn(true);
        when(guestCartService.createGuestCart()).thenReturn(mockGuestCartId);

        mockMvc.perform(post("/api/v1/guest-cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.guestCartId").value(mockGuestCartId));
    }

    @Test
    public void createGuestCart_RateLimited_ShouldReturn429() throws Exception {
        when(guestCartService.allowCreateGuestCart(any(), anyInt())).thenReturn(false);

        mockMvc.perform(post("/api/v1/guest-cart"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void replaceGuestCart_ShouldReturnSuccess() throws Exception {
        String guestCartId = "guest_123456";
        GuestCartUpdateRequest request = GuestCartUpdateRequest.builder()
                .items(Collections.emptyList())
                .build();

        doNothing().when(guestCartService).replaceItems(eq(guestCartId), any(GuestCartUpdateRequest.class));

        mockMvc.perform(put("/api/v1/guest-cart/{guestCartId}", guestCartId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã cập nhật guest cart"));
    }

    @Test
    public void deleteGuestCart_ShouldReturnSuccess() throws Exception {
        String guestCartId = "guest_123456";

        doNothing().when(guestCartService).deleteGuestCart(guestCartId);

        mockMvc.perform(delete("/api/v1/guest-cart/{guestCartId}", guestCartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã xóa guest cart"));
    }
}
