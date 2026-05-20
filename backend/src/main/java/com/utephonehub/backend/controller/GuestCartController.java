package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.guestcart.GuestCartUpdateRequest;
import com.utephonehub.backend.dto.response.guestcart.GuestCartSessionResponse;
import com.utephonehub.backend.service.IGuestCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guest-cart")
@RequiredArgsConstructor
@Tag(name = "Guest Cart", description = "API giỏ hàng tạm cho khách (lưu Redis)")
public class GuestCartController {

    private final IGuestCartService guestCartService;

    // Minimal anti-abuse: limit create requests per IP per minute.
    private static final int CREATE_RATE_LIMIT_PER_MINUTE = 30;

    @PostMapping
    @Operation(summary = "Tạo guest cart", description = "Tạo một guestCartId (lưu trong Redis) để FE đồng bộ giỏ tạm theo phiên")
    public ResponseEntity<ApiResponse<GuestCartSessionResponse>> createGuestCart(HttpServletRequest request) {
        String ip = request != null ? request.getRemoteAddr() : null;
        if (!guestCartService.allowCreateGuestCart(ip, CREATE_RATE_LIMIT_PER_MINUTE)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests"));
        }

        String guestCartId = guestCartService.createGuestCart();
        return ResponseEntity.ok(ApiResponse.success(GuestCartSessionResponse.builder().guestCartId(guestCartId).build()));
    }

    @PutMapping("/{guestCartId}")
    @Operation(summary = "Cập nhật guest cart", description = "Replace toàn bộ items của guest cart trong Redis (cho phép empty để clear)")
    public ResponseEntity<ApiResponse<Void>> replaceGuestCart(
            @PathVariable String guestCartId,
            @Valid @RequestBody GuestCartUpdateRequest request
    ) {
        guestCartService.replaceItems(guestCartId, request);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật guest cart", null));
    }

    @DeleteMapping("/{guestCartId}")
    @Operation(summary = "Xóa guest cart", description = "Xóa guest cart trong Redis")
    public ResponseEntity<ApiResponse<Void>> deleteGuestCart(@PathVariable String guestCartId) {
        guestCartService.deleteGuestCart(guestCartId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa guest cart", null));
    }
}
