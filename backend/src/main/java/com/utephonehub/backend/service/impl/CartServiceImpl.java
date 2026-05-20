package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.cart.AddToCartRequest;
import com.utephonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.utephonehub.backend.dto.request.cart.UpdateCartItemRequest;
import com.utephonehub.backend.dto.response.cart.CartResponse;
import com.utephonehub.backend.dto.response.cart.MergeCartResponse;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.event.CartUpdatedEvent;
import com.utephonehub.backend.exception.*;
import com.utephonehub.backend.repository.CartItemRepository;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.ICartService;
import com.utephonehub.backend.service.IGuestCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final com.utephonehub.backend.repository.OrderRepository orderRepository;
    private final com.utephonehub.backend.mapper.CartMapper cartMapper;
    private final IGuestCartService guestCartService;

    private static final int MAX_QUANTITY_PER_PRODUCT = 10;
    private static final int BATCH_DELETE_SIZE = 50;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cart", key = "#userId", unless = "#result == null")
    public CartResponse getCurrentCart(Long userId) {
        log.info("Getting cart for user: {}", userId);
        
        // Get cart (read-only)
        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        
        // If cart doesn't exist, create it in a separate write transaction
        Cart cart = cartOpt.orElseGet(() -> createCartForUser(userId));

        // Auto-remove items with deleted products (UC 1.2 Alternate 4.A.1)
        List<CartItem> invalidItems = cart.getItemsInternal().stream()
                .filter(item -> item.getProduct() == null || item.getProduct().getId() == null)
                .toList();
        
        if (!invalidItems.isEmpty()) {
            log.warn("Found {} invalid items in cart, removing...", invalidItems.size());
            // Need to remove invalid items in a write transaction
            removeInvalidItems(userId, invalidItems);
            // Re-fetch cart after cleanup
            cart = cartRepository.findByUserIdWithItems(userId)
                    .orElseGet(() -> createCartForUser(userId));
        }

        return cartMapper.toResponse(cart);
    }
    
    /**
     * Create a new cart for user in a separate write transaction
     * This method is called from read-only getCurrentCart when cart doesn't exist
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Cart createCartForUser(Long userId) {
        log.info("Creating new cart for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        Cart newCart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
        return cartRepository.save(newCart);
    }
    
    /**
     * Remove invalid items from cart in a separate write transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void removeInvalidItems(Long userId, List<CartItem> invalidItems) {
        log.info("Removing {} invalid items from cart for user: {}", invalidItems.size(), userId);
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));
        
        invalidItems.forEach(cart::removeItem);
        cartItemRepository.deleteAll(invalidItems);
        cartRepository.save(cart);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "cart", key = "#userId")
    @Retryable(
        retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding product {} to cart for user: {}", request.getProductId(), userId);

        try {
            // Validate initial quantity
            if (request.getQuantity() > MAX_QUANTITY_PER_PRODUCT) {
                throw new MaxQuantityExceededException(MAX_QUANTITY_PER_PRODUCT, request.getQuantity());
            }

            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

            // Validate product exists and is active (UC 1.1 - step 4)
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

            if (!Boolean.TRUE.equals(product.getStatus())) {
                throw new ResourceNotFoundException("Sản phẩm không tồn tại");
            }

            // Check stock availability
            if (getTotalStockQuantity(product) < request.getQuantity()) {
                throw new OutOfStockException(
                    product.getId(),
                    product.getName(),
                    request.getQuantity(),
                    getTotalStockQuantity(product)
                );
            }

            // Get or create cart
            Cart cart = cartRepository.findByUserIdWithItems(userId)
                    .orElseGet(() -> {
                        Cart newCart = Cart.builder()
                                .user(user)
                                .items(new ArrayList<>())
                                .build();
                        return cartRepository.save(newCart);
                    });

            // Check if product already exists in cart
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Update existing item quantity
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity() + request.getQuantity();

                // Validate max quantity first
                if (newQuantity > MAX_QUANTITY_PER_PRODUCT) {
                    throw new MaxQuantityExceededException(MAX_QUANTITY_PER_PRODUCT, newQuantity);
                }

                // Then check stock availability
                if (newQuantity > getTotalStockQuantity(product)) {
                    throw new OutOfStockException(
                        product.getId(),
                        product.getName(),
                        newQuantity,
                        getTotalStockQuantity(product)
                    );
                }

                item.setQuantity(newQuantity);
                cartItemRepository.save(item);
                
                // Publish event
                publishCartEvent(cart, "UPDATED", product, newQuantity);
            } else {
                // Add new item to cart
                int quantity = request.getQuantity();

                // Validate max quantity first
                if (quantity > MAX_QUANTITY_PER_PRODUCT) {
                    throw new MaxQuantityExceededException(MAX_QUANTITY_PER_PRODUCT, quantity);
                }

                // Then check stock availability
                if (quantity > getTotalStockQuantity(product)) {
                    throw new OutOfStockException(
                        product.getId(),
                        product.getName(),
                        quantity,
                        getTotalStockQuantity(product)
                    );
                }

                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build();
                cartItemRepository.save(newItem);
                cart.addItem(newItem);
                
                // Publish event
                publishCartEvent(cart, "ADDED", product, request.getQuantity());
            }

            cartRepository.save(cart);
            return cartMapper.toResponse(cart);
            
        } catch (OptimisticLockingFailureException | StaleObjectStateException ex) {
            log.warn("Optimistic locking conflict detected, retrying... Attempt: {}", ex.getClass().getSimpleName());
            throw ex; // Let @Retryable handle it
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "cart", key = "#userId")
    @Retryable(
        retryFor = {OptimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class, StaleObjectStateException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} for user: {}", cartItemId, userId);

        try {
            // Get cart item and validate ownership
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại"));

            if (!cartItem.getCart().getUser().getId().equals(userId)) {
                throw new UnauthorizedException("Bạn không có quyền thực hiện thao tác này");
            }

            // If quantity is 0, remove item
            if (request.getQuantity() == 0) {
                return removeCartItem(userId, cartItemId);
            }

            // Validate max quantity first
            if (request.getQuantity() > MAX_QUANTITY_PER_PRODUCT) {
                throw new MaxQuantityExceededException(MAX_QUANTITY_PER_PRODUCT, request.getQuantity());
            }

            // Then check stock availability
            Product product = cartItem.getProduct();
            if (getTotalStockQuantity(product) < request.getQuantity()) {
                throw new OutOfStockException(
                    product.getId(),
                    product.getName(),
                    request.getQuantity(),
                    getTotalStockQuantity(product)
                );
            }

            // Update quantity
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);

            Cart cart = cartItem.getCart();
            cartRepository.save(cart);
            
            // Publish event
            publishCartEvent(cart, "UPDATED", product, request.getQuantity());
            
            return cartMapper.toResponse(cart);
            
        } catch (OptimisticLockingFailureException | StaleObjectStateException ex) {
            log.warn("Optimistic locking conflict detected, retrying...");
            throw ex;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        log.info("Removing cart item {} for user: {}", cartItemId, userId);

        // Get cart item and validate existence
        Optional<CartItem> optionalCartItem = cartItemRepository.findById(cartItemId);

        // EF1 – CartItem already removed elsewhere: return current cart state
        if (optionalCartItem.isEmpty()) {
            log.warn("Cart item {} not found when trying to remove. Returning current cart state for user {}.",
                cartItemId, userId);

            Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

            return cartMapper.toResponse(cart);
        }

        CartItem cartItem = optionalCartItem.get();

        // Validate ownership
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền thực hiện thao tác này");
        }

        // Remove item from cart
        Cart cart = cartItem.getCart();
        Product product = cartItem.getProduct();
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        
        // Publish event
        publishCartEvent(cart, "REMOVED", product, 0);

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CartResponse clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        // EF3 – Check for pending orders before clearing
        boolean hasPendingOrder = orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .anyMatch(order -> order.getStatus() == com.utephonehub.backend.enums.OrderStatus.PENDING);

        if (hasPendingOrder) {
            log.warn("User {} attempted to clear cart while having pending orders", userId);
            throw new BadRequestException("Không thể xóa giỏ hàng vì đang có đơn hàng đang xử lý");
        }

        // Get cart and items list
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        List<CartItem> items = new ArrayList<>(cart.getItems());
        int totalItems = items.size();
        
        // Batch delete for large carts (UC 1.5 Exception 8.1)
        if (totalItems > BATCH_DELETE_SIZE) {
            log.info("Large cart detected ({}+ items), using batch delete", BATCH_DELETE_SIZE);
            
            for (int i = 0; i < totalItems; i += BATCH_DELETE_SIZE) {
                int toIndex = Math.min(i + BATCH_DELETE_SIZE, totalItems);
                List<CartItem> batch = items.subList(i, toIndex);
                cartItemRepository.deleteAll(batch);
                log.info("Deleted batch: {}/{}", toIndex, totalItems);
            }
        } else {
            // Normal delete
            cartItemRepository.deleteAll(items);
        }
        
        // Clear all items and save cart
        cart.clearItems();
        cartRepository.save(cart);
        
        // Publish event
        publishCartEvent(cart, "CLEARED", null, 0);

        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public MergeCartResponse mergeGuestCart(Long userId, MergeGuestCartRequest request) {
        log.info("Merging guest cart for user: {}", userId);

        if (request == null) {
            throw new BadRequestException("Dữ liệu đồng bộ giỏ hàng không hợp lệ");
        }

        // If guestCartId is provided, prefer loading items from Redis guest cart.
        // Backward-compatible: if no guestCartId, use guestCartItems payload as before.
        if (request.getGuestCartId() != null && !request.getGuestCartId().isBlank()) {
            request.setGuestCartItems(guestCartService.getItemsForMerge(request.getGuestCartId()));
        }

        if (request.getGuestCartItems() == null || request.getGuestCartItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng tạm không được để trống");
        }

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Get or create cart
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });

        int mergedCount = 0;
        int skippedCount = 0;

        // Process each guest cart item
        for (MergeGuestCartRequest.GuestCartItem guestItem : request.getGuestCartItems()) {
            try {
                // Validate product exists and is active
                Product product = productRepository.findById(guestItem.getProductId())
                        .orElse(null);

                if (product == null || !Boolean.TRUE.equals(product.getStatus())) {
                    log.warn("Product {} not found or inactive, skipping", guestItem.getProductId());
                    skippedCount++;
                    continue;
                }

                // Check stock availability
                int totalStock = getTotalStockQuantity(product);
                if (totalStock < guestItem.getQuantity()) {
                    log.warn("Product {} out of stock, skipping", guestItem.getProductId());
                    skippedCount++;
                    continue;
                }

                // Check if product already in cart
                Optional<CartItem> existingItem = cart.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()))
                        .findFirst();

                if (existingItem.isPresent()) {
                    // Merge quantity if already exists
                    CartItem item = existingItem.get();
                    int newQuantity = Math.min(
                        item.getQuantity() + guestItem.getQuantity(),
                        MAX_QUANTITY_PER_PRODUCT
                    );
                    
                    if (newQuantity <= getTotalStockQuantity(product)) {
                        item.setQuantity(newQuantity);
                        cartItemRepository.save(item);
                        mergedCount++;
                    } else {
                        skippedCount++;
                    }
                } else {
                    // Add new item if not exists
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(Math.min(guestItem.getQuantity(), MAX_QUANTITY_PER_PRODUCT))
                            .build();
                    cartItemRepository.save(newItem);
                    cart.addItem(newItem);
                    mergedCount++;
                }
            } catch (Exception ex) {
                log.error("Error merging guest cart item: {}", guestItem.getProductId(), ex);
                skippedCount++;
            }
        }

        // Save cart
        cartRepository.save(cart);

        // Cleanup guest cart in Redis after merge (best-effort)
        try {
            if (request.getGuestCartId() != null && !request.getGuestCartId().isBlank()) {
                guestCartService.deleteGuestCart(request.getGuestCartId());
            }
        } catch (Exception ex) {
            log.warn("Failed to delete guest cart {} after merge", request.getGuestCartId(), ex);
        }
        
        String message = String.format("Đã đồng bộ %d sản phẩm từ giỏ hàng khách", mergedCount);
        if (skippedCount > 0) {
            message += String.format(". %d sản phẩm đã bị bỏ qua do hết hàng hoặc không tồn tại", skippedCount);
        }

        return MergeCartResponse.builder()
                .cart(cartMapper.toResponse(cart))
                .mergedItemsCount(mergedCount)
                .skippedItemsCount(skippedCount)
                .message(message)
                .build();
    }

    private void publishCartEvent(Cart cart, String eventType, Product product, Integer quantity) {
        try {
            CartResponse cartResponse = cartMapper.toResponse(cart);
            
            CartUpdatedEvent event = CartUpdatedEvent.builder()
                    .cartId(cart.getId())
                    .userId(cart.getUser().getId())
                    .eventType(eventType)
                    .productId(product != null ? product.getId() : null)
                    .productName(product != null ? product.getName() : null)
                    .quantity(quantity)
                    .totalAmount(cartResponse.getTotalAmount())
                    .itemCount(cartResponse.getItemCount())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            eventPublisher.publishEvent(event);
            log.debug("Published CartUpdatedEvent: {}", eventType);
        } catch (Exception ex) {
            log.error("Error publishing cart event", ex);
            // Don't fail the transaction due to event publishing failure
        }
    }
    
    /**
     * Helper method to get total stock quantity from all active product templates
     * @param product Product entity with templates
     * @return Total stock quantity across all active templates
     */
    private Integer getTotalStockQuantity(Product product) {
        return product.getTemplates().stream()
                .filter(template -> template.getStatus()) // Only active templates
                .mapToInt(template -> template.getStockQuantity())
                .sum();
    }
}
