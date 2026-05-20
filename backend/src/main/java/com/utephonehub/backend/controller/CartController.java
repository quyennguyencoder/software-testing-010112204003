package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.cart.AddToCartRequest;
import com.utephonehub.backend.dto.request.cart.MergeGuestCartRequest;
import com.utephonehub.backend.dto.request.cart.UpdateCartItemRequest;
import com.utephonehub.backend.dto.response.cart.CartResponse;
import com.utephonehub.backend.dto.response.cart.MergeCartResponse;
import com.utephonehub.backend.service.ICartService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API quản lý giỏ hàng")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final ICartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @Operation(summary = "Xem giỏ hàng", description = "Lấy thông tin giỏ hàng của người dùng hiện tại")
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart(HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        CartResponse cart = cartService.getCurrentCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng", description = "Thêm sản phẩm mới hoặc cộng dồn số lượng nếu đã có")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest addToCartRequest,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        CartResponse cart = cartService.addToCart(userId, addToCartRequest);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm vào giỏ hàng", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm", description = "Thay đổi số lượng sản phẩm trong giỏ. Nếu quantity = 0 thì xóa sản phẩm")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest updateCartItemRequest,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        CartResponse cart = cartService.updateCartItem(userId, itemId, updateCartItemRequest);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật giỏ hàng", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng", description = "Xóa một sản phẩm cụ thể khỏi giỏ")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long itemId,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        CartResponse cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa sản phẩm", cart));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Xóa toàn bộ giỏ hàng", description = "Xóa tất cả sản phẩm trong giỏ hàng")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        CartResponse cart = cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa toàn bộ giỏ hàng", cart));
    }

    @PostMapping("/merge")
    @Operation(summary = "Đồng bộ giỏ hàng tạm", description = "Merge guest cart từ localStorage vào user cart sau khi login")
    public ResponseEntity<ApiResponse<MergeCartResponse>> mergeGuestCart(
            @Valid @RequestBody MergeGuestCartRequest mergeGuestCartRequest,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        MergeCartResponse response = cartService.mergeGuestCart(userId, mergeGuestCartRequest);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }
}
