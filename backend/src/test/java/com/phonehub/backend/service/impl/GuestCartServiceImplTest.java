package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.phonehub.backend.dto.request.guestcart.GuestCartItemRequest;
import com.phonehub.backend.dto.request.guestcart.GuestCartUpdateRequest;
import com.phonehub.backend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestCartServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private GuestCartServiceImpl guestCartService;

    @BeforeEach
    void setUp() {
        // Mock redis operations when needed
    }

    @Test
    void allowCreateGuestCart_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

        boolean result = guestCartService.allowCreateGuestCart("127.0.0.1", 10);

        assertTrue(result);
        verify(valueOperations, times(1)).increment(anyString());
    }

    @Test
    void allowCreateGuestCart_RateLimitExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(11L);

        boolean result = guestCartService.allowCreateGuestCart("127.0.0.1", 10);

        assertFalse(result);
    }

    @Test
    void createGuestCart_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), any(), any(Duration.class));

        String guestCartId = guestCartService.createGuestCart();

        assertNotNull(guestCartId);
        assertFalse(guestCartId.isBlank());
        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void replaceItems_Success() {
        GuestCartUpdateRequest request = new GuestCartUpdateRequest();
        GuestCartItemRequest item = new GuestCartItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(Collections.singletonList(item));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), any(), any(Duration.class));

        guestCartService.replaceItems("cart123", request);

        verify(valueOperations, times(1)).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void replaceItems_NullId_ThrowsException() {
        GuestCartUpdateRequest request = new GuestCartUpdateRequest();

        assertThrows(BadRequestException.class, () -> guestCartService.replaceItems(null, request));
    }

    @Test
    void deleteGuestCart_Success() {
        when(redisTemplate.delete(anyString())).thenReturn(true);

        guestCartService.deleteGuestCart("cart123");

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void getItemsForMerge_Success() {
        GuestCartServiceImpl.GuestCartRedisItem redisItem = GuestCartServiceImpl.GuestCartRedisItem.builder()
                .productId(1L)
                .quantity(2)
                .build();
        GuestCartServiceImpl.GuestCartRedisModel redisModel = GuestCartServiceImpl.GuestCartRedisModel.builder()
                .items(Collections.singletonList(redisItem))
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(redisModel);

        List<MergeGuestCartRequest.GuestCartItem> result = guestCartService.getItemsForMerge("cart123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(2, result.get(0).getQuantity());
    }
}
