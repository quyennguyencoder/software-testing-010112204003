package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.utephonehub.backend.dto.request.guestcart.GuestCartItemRequest;
import com.utephonehub.backend.dto.request.guestcart.GuestCartUpdateRequest;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.service.IGuestCartService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestCartServiceImpl implements IGuestCartService {

    private static final String KEY_PREFIX = "guest-cart:";
    private static final String RATE_LIMIT_PREFIX = "rl:guest-cart:create:";
    private static final Duration TTL = Duration.ofHours(24);
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean allowCreateGuestCart(String ipAddress, int limitPerMinute) {
        if (limitPerMinute <= 0) return true;
        String ip = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress.trim();
        String key = RATE_LIMIT_PREFIX + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, RATE_LIMIT_WINDOW);
            }
            return count == null || count <= (long) limitPerMinute;
        } catch (Exception e) {
            // Best-effort: if Redis hiccups, do not break guest flow.
            return true;
        }
    }

    @Override
    public String createGuestCart() {
        String id = UUID.randomUUID().toString();
        GuestCartRedisModel model = GuestCartRedisModel.builder()
                .items(new ArrayList<>())
                .updatedAt(Instant.now().toEpochMilli())
                .build();

        String key = key(id);
        redisTemplate.opsForValue().set(key, model, TTL);
        return id;
    }

    @Override
    public void replaceItems(String guestCartId, GuestCartUpdateRequest request) {
        if (guestCartId == null || guestCartId.isBlank()) {
            throw new BadRequestException("guestCartId không hợp lệ");
        }

        List<GuestCartItemRequest> incoming = request != null ? request.getItems() : null;
        List<GuestCartRedisItem> items = new ArrayList<>();

        if (incoming != null) {
            for (GuestCartItemRequest it : incoming) {
                if (it == null || it.getProductId() == null) continue;
                Integer qty = it.getQuantity();
                if (qty == null) qty = 1;
                int normalizedQty = Math.max(1, Math.min(10, qty));
                items.add(GuestCartRedisItem.builder()
                        .productId(it.getProductId())
                        .quantity(normalizedQty)
                        .build());
            }
        }

        GuestCartRedisModel model = GuestCartRedisModel.builder()
                .items(items)
                .updatedAt(Instant.now().toEpochMilli())
                .build();

        redisTemplate.opsForValue().set(key(guestCartId), model, TTL);
    }

    @Override
    public void deleteGuestCart(String guestCartId) {
        if (guestCartId == null || guestCartId.isBlank()) return;
        redisTemplate.delete(key(guestCartId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MergeGuestCartRequest.GuestCartItem> getItemsForMerge(String guestCartId) {
        if (guestCartId == null || guestCartId.isBlank()) {
            return Collections.emptyList();
        }

        Object raw = redisTemplate.opsForValue().get(key(guestCartId));
        if (!(raw instanceof GuestCartRedisModel model)) {
            return Collections.emptyList();
        }

        List<GuestCartRedisItem> items = model.getItems();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<MergeGuestCartRequest.GuestCartItem> out = new ArrayList<>();
        for (GuestCartRedisItem it : items) {
            if (it == null || it.getProductId() == null) continue;
            Integer qty = it.getQuantity();
            if (qty == null) qty = 1;
            out.add(MergeGuestCartRequest.GuestCartItem.builder()
                    .productId(it.getProductId())
                    .quantity(Math.max(1, Math.min(10, qty)))
                    .build());
        }
        return out;
    }

    private String key(String guestCartId) {
        return KEY_PREFIX + guestCartId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestCartRedisModel {
        private List<GuestCartRedisItem> items;
        private Long updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestCartRedisItem {
        private Long productId;
        private Integer quantity;
    }
}
