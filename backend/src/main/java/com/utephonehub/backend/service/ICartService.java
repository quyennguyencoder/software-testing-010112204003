package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.cart.AddToCartRequest;
import com.utephonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.utephonehub.backend.dto.request.cart.UpdateCartItemRequest;
import com.utephonehub.backend.dto.response.cart.CartResponse;
import com.utephonehub.backend.dto.response.cart.MergeCartResponse;

public interface ICartService {

    /**
     * Get current user's cart (UC 1.2)
     */
    CartResponse getCurrentCart(Long userId);

    /**
     * Add product to cart (UC 1.1)
     */
    CartResponse addToCart(Long userId, AddToCartRequest request);

    /**
     * Update cart item quantity (UC 1.3)
     */
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    /**
     * Remove product from cart (UC 1.4)
     */
    CartResponse removeCartItem(Long userId, Long cartItemId);

    /**
     * Clear all items in cart (UC 1.5)
     */
    CartResponse clearCart(Long userId);

    /**
     * Merge guest cart with user cart after login (UC 1.1 Alternate 6.A.1)
     */
    MergeCartResponse mergeGuestCart(Long userId, MergeGuestCartRequest request);
}
