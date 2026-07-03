package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.cart.AddToCartRequest;
import com.phonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.phonehub.backend.dto.request.cart.UpdateCartItemRequest;
import com.phonehub.backend.dto.response.cart.CartResponse;
import com.phonehub.backend.dto.response.cart.MergeCartResponse;
import com.phonehub.backend.entity.Cart;
import com.phonehub.backend.entity.CartItem;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.exception.MaxQuantityExceededException;
import com.phonehub.backend.exception.OutOfStockException;
import com.phonehub.backend.mapper.CartMapper;
import com.phonehub.backend.repository.CartItemRepository;
import com.phonehub.backend.repository.CartRepository;
import com.phonehub.backend.repository.OrderRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.service.intf.IGuestCartService;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private IGuestCartService guestCartService;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private ProductTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);

        testTemplate = new ProductTemplate();
        testTemplate.setId(1L);
        testTemplate.setStatus(true);
        testTemplate.setStockQuantity(20);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setStatus(true);
        testProduct.setTemplates(Collections.singletonList(testTemplate));
    }

    @Test
    void getCurrentCart_Success() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.getCurrentCart(1L);

        assertNotNull(result);
        verify(cartRepository, times(1)).findByUserIdWithItems(1L);
    }

    @Test
    void createCartForUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.createCartForUser(1L);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCurrentCart_WithInvalidItems_ShouldRemoveThemAndReturnCart() {
        CartItem invalidItem = new CartItem();
        invalidItem.setProduct(null);
        testCart.addItem(invalidItem);

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart), Optional.of(testCart));

        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.getCurrentCart(1L);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(cartRepository, times(3)).findByUserIdWithItems(1L);
    }

    @Test
    void addToCart_Success() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.addToCart(1L, request);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_MaxQuantityExceeded_ThrowsException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(15); // > 10

        assertThrows(MaxQuantityExceededException.class, () -> cartService.addToCart(1L, request));
    }

    @Test
    void addToCart_OutOfStock_ThrowsException() {
        testTemplate.setStockQuantity(1); // Only 1 in stock
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(OutOfStockException.class, () -> cartService.addToCart(1L, request));
    }

    @Test
    void updateCartItem_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(1);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(3);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.updateCartItem(1L, 1L, request);

        assertNotNull(result);
        assertEquals(3, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    void removeCartItem_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);
        testCart.addItem(cartItem);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.removeCartItem(1L, 1L);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    // ============ ADDITIONAL TESTS FOR FULL COVERAGE ============

    @Test
    void getCurrentCart_CartNotFound_ShouldCreateCart() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.getCurrentCart(1L);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void createCartForUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> cartService.createCartForUser(99L));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(99L);
        request.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> cartService.addToCart(1L, request));
    }

    @Test
    void addToCart_ProductInactive_ThrowsException() {
        testProduct.setStatus(false);
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(Exception.class, () -> cartService.addToCart(1L, request));
    }

    @Test
    void addToCart_ProductAlreadyInCart_ShouldUpdateQuantity() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(3);
        testCart.addItem(existingItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.addToCart(1L, request);

        assertNotNull(result);
        assertEquals(5, existingItem.getQuantity());
        verify(cartItemRepository, times(1)).save(existingItem);
    }

    @Test
    void addToCart_UpdateQuantityExceedsMax_ThrowsException() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(8); // 3 + 8 = 11 > 10

        CartItem existingItem = new CartItem();
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(3);
        testCart.addItem(existingItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));

        assertThrows(MaxQuantityExceededException.class, () -> cartService.addToCart(1L, request));
    }

    @Test
    void addToCart_NewProduct_ShouldAddItem() {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.addToCart(1L, request);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_QuantityZero_ShouldRemove() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.updateCartItem(1L, 1L, request);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void updateCartItem_Unauthorized_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        Cart otherCart = new Cart();
        otherCart.setUser(otherUser);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(otherCart);
        cartItem.setProduct(testProduct);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(Exception.class, () -> cartService.updateCartItem(1L, 1L, request));
    }

    @Test
    void updateCartItem_QuantityExceedsMax_ThrowsException() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(1);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(15); // > 10

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(MaxQuantityExceededException.class, () -> cartService.updateCartItem(1L, 1L, request));
    }

    @Test
    void updateCartItem_QuantityExceedsStock_ThrowsException() {
        testTemplate.setStockQuantity(5);
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(testCart);
        cartItem.setProduct(testProduct);
        cartItem.setQuantity(1);

        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(10); // > 5 stock

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(OutOfStockException.class, () -> cartService.updateCartItem(1L, 1L, request));
    }

    @Test
    void removeCartItem_ItemNotFound_ShouldReturnCurrentCart() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.removeCartItem(1L, 1L);

        assertNotNull(result);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void removeCartItem_Unauthorized_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        Cart otherCart = new Cart();
        otherCart.setUser(otherUser);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(otherCart);
        cartItem.setProduct(testProduct);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        assertThrows(Exception.class, () -> cartService.removeCartItem(1L, 1L));
    }

    // ============ CLEARCART TESTS ============

    @Test
    void clearCart_Success_ShouldClearAllItems() {
        testCart.addItem(new CartItem());
        testCart.addItem(new CartItem());

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.clearCart(1L);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).deleteAll(any());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    void clearCart_WithPendingOrder_ThrowsException() {
        Order pendingOrder = new Order();
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.singletonList(pendingOrder));

        assertThrows(Exception.class, () -> cartService.clearCart(1L));
        verify(cartItemRepository, never()).deleteAll(any());
    }

    @Test
    void clearCart_WithBatchDelete_ShouldDeleteInBatches() {
        // Create cart with 55 items (> 50 batch size)
        for (int i = 0; i < 55; i++) {
            testCart.addItem(new CartItem());
        }

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        
        CartResponse response = new CartResponse();
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        CartResponse result = cartService.clearCart(1L);

        assertNotNull(result);
        verify(cartItemRepository, times(2)).deleteAll(any()); // 2 batches (50 + 5)
    }

    @Test
    void clearCart_CartNotFound_ThrowsException() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> cartService.clearCart(1L));
    }

    // ============ MERGEGUESTCART TESTS ============

    @Test
    void mergeGuestCart_Success_ShouldMergeItems() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.valueOf(1000L));
        response.setItemCount(1);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getMergedItemsCount());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void mergeGuestCart_WithGuestCartId_ShouldFetchFromRedis() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        request.setGuestCartId("guest-123");
        
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);

        when(guestCartService.getItemsForMerge("guest-123")).thenReturn(Collections.singletonList(guestItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.valueOf(1000));
        response.setItemCount(1);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getMergedItemsCount());
        verify(guestCartService, times(1)).getItemsForMerge("guest-123");
        verify(guestCartService, times(1)).deleteGuestCart("guest-123");
    }

    @Test
    void mergeGuestCart_NullRequest_ThrowsException() {
        assertThrows(Exception.class, () -> cartService.mergeGuestCart(1L, null));
    }

    @Test
    void mergeGuestCart_EmptyGuestCart_ThrowsException() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        request.setGuestCartItems(Collections.emptyList());

        assertThrows(Exception.class, () -> cartService.mergeGuestCart(1L, request));
    }

    @Test
    void mergeGuestCart_ProductNotFound_ShouldSkip() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(99L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.ZERO);
        response.setItemCount(0);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getMergedItemsCount());
        assertEquals(1, result.getSkippedItemsCount());
    }

    @Test
    void mergeGuestCart_ProductInactive_ShouldSkip() {
        testProduct.setStatus(false);
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.ZERO);
        response.setItemCount(0);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getMergedItemsCount());
        assertEquals(1, result.getSkippedItemsCount());
    }

    @Test
    void mergeGuestCart_OutOfStock_ShouldSkip() {
        testTemplate.setStockQuantity(1);
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(5);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.ZERO);
        response.setItemCount(0);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getMergedItemsCount());
        assertEquals(1, result.getSkippedItemsCount());
    }

    @Test
    void mergeGuestCart_ExistingItemExceedsStock_ShouldSkip() {
        CartItem existingItem = new CartItem();
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(1);
        testCart.addItem(existingItem);
        testTemplate.setStockQuantity(2);

        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(5);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.ZERO);
        response.setItemCount(0);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(0, result.getMergedItemsCount());
        assertEquals(1, result.getSkippedItemsCount());
    }

    @Test
    void mergeGuestCart_DeleteGuestCartThrows_ShouldStillReturnResponse() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        request.setGuestCartId("guest-123");

        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(guestCartService.getItemsForMerge("guest-123")).thenReturn(Collections.singletonList(guestItem));
        doThrow(new RuntimeException("redis down")).when(guestCartService).deleteGuestCart("guest-123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());

        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.valueOf(1000));
        response.setItemCount(1);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getMergedItemsCount());
        verify(guestCartService, times(1)).deleteGuestCart("guest-123");
    }

    @Test
    void mergeGuestCart_ProductAlreadyInCart_ShouldMergeQuantity() {
        CartItem existingItem = new CartItem();
        existingItem.setProduct(testProduct);
        existingItem.setQuantity(3);
        testCart.addItem(existingItem);

        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(5);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.valueOf(2000));
        response.setItemCount(1);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getMergedItemsCount());
        assertEquals(8, existingItem.getQuantity()); // 3 + 5
    }

    @Test
    void mergeGuestCart_UserNotFound_ThrowsException() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> cartService.mergeGuestCart(1L, request));
    }

    @Test
    void mergeGuestCart_CreateCartIfNotExists() {
        MergeGuestCartRequest request = new MergeGuestCartRequest();
        MergeGuestCartRequest.GuestCartItem guestItem = new MergeGuestCartRequest.GuestCartItem();
        guestItem.setProductId(1L);
        guestItem.setQuantity(2);
        request.setGuestCartItems(Collections.singletonList(guestItem));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        
        CartResponse response = new CartResponse();
        response.setTotalAmount(BigDecimal.valueOf(1000));
        response.setItemCount(1);
        when(cartMapper.toResponse(testCart)).thenReturn(response);

        MergeCartResponse result = cartService.mergeGuestCart(1L, request);

        assertNotNull(result);
        verify(cartRepository, times(2)).save(any(Cart.class));
    }
}
