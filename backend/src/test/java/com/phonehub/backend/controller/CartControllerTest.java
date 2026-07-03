package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.cart.AddToCartRequest;
import com.phonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.phonehub.backend.dto.request.cart.UpdateCartItemRequest;
import com.phonehub.backend.dto.response.cart.CartResponse;
import com.phonehub.backend.dto.response.cart.MergeCartResponse;
import com.phonehub.backend.service.intf.ICartService;
import com.phonehub.backend.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICartService cartService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CartController cartController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    public void getCurrentCart_ShouldReturnCart() throws Exception {
        Long userId = 1L;
        CartResponse cartResponse = CartResponse.builder()
                .id(10L)
                .totalAmount(BigDecimal.TEN)
                .itemCount(1)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.getCurrentCart(userId)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    public void addToCart_ShouldReturnUpdatedCart() throws Exception {
        Long userId = 1L;
        AddToCartRequest request = AddToCartRequest.builder().productId(2L).quantity(3).build();
        CartResponse cartResponse = CartResponse.builder().id(10L).itemCount(3).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.addToCart(eq(userId), any(AddToCartRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã thêm vào giỏ hàng"));
    }

    @Test
    public void updateCartItem_ShouldReturnUpdatedCart() throws Exception {
        Long userId = 1L;
        Long itemId = 5L;
        UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(2).build();
        CartResponse cartResponse = CartResponse.builder().id(10L).itemCount(2).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.updateCartItem(eq(userId), eq(itemId), any(UpdateCartItemRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(put("/api/v1/cart/items/{itemId}", itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã cập nhật giỏ hàng"));
    }

    @Test
    public void removeCartItem_ShouldReturnUpdatedCart() throws Exception {
        Long userId = 1L;
        Long itemId = 5L;
        CartResponse cartResponse = CartResponse.builder().id(10L).itemCount(0).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.removeCartItem(userId, itemId)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã xóa sản phẩm"));
    }

    @Test
    public void clearCart_ShouldReturnEmptyCart() throws Exception {
        Long userId = 1L;
        CartResponse cartResponse = CartResponse.builder().id(10L).itemCount(0).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.clearCart(userId)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã xóa toàn bộ giỏ hàng"));
    }

    @Test
    public void mergeGuestCart_ShouldReturnMergeResponse() throws Exception {
        Long userId = 1L;
        MergeGuestCartRequest request = MergeGuestCartRequest.builder()
                .guestCartItems(Collections.singletonList(
                        MergeGuestCartRequest.GuestCartItem.builder().productId(2L).quantity(2).build()
                ))
                .build();
        MergeCartResponse response = MergeCartResponse.builder()
                .mergedItemsCount(1)
                .message("Đồng bộ thành công")
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(cartService.mergeGuestCart(eq(userId), any(MergeGuestCartRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/merge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đồng bộ thành công"));
    }
}
