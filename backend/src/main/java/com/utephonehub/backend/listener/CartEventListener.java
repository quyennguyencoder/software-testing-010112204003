package com.utephonehub.backend.listener;

import com.utephonehub.backend.event.CartUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for cart updates.
 * Can be extended to send WebSocket notifications, update cache, etc.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartEventListener {

    private final CacheManager cacheManager;
    
    // TODO: Inject WebSocket MessageSendingOperations when implementing realtime sync
    // private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void handleCartUpdatedEvent(CartUpdatedEvent event) {
        log.info("Cart updated event received: userId={}, eventType={}, productId={}", 
                event.getUserId(), event.getEventType(), event.getProductId());

        // Invalidate cache for the user
        try {
            if (cacheManager.getCache("cart") != null) {
                cacheManager.getCache("cart").evict(event.getUserId());
                log.debug("Cache evicted for user: {}", event.getUserId());
            }
        } catch (Exception ex) {
            log.error("Error evicting cache", ex);
        }

        // TODO: Send WebSocket notification to user
        // messagingTemplate.convertAndSendToUser(
        //     event.getUserId().toString(),
        //     "/queue/cart",
        //     event
        // );

        // TODO: Log to analytics/monitoring service
        // analyticsService.trackCartEvent(event);
    }
}
