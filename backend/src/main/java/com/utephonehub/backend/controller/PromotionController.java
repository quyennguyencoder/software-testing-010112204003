package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.PromotionRequest;
import com.utephonehub.backend.dto.response.PromotionResponse;
import com.utephonehub.backend.service.IPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Promotion Controller", description = "Quản lý Khuyến mãi & Voucher (M09)")
public class PromotionController {

    private final IPromotionService promotionService;

    // ==========================================
    // ACTOR: ADMINISTRATOR (Path: /api/v1/admin/promotions)
    // ==========================================

    @PostMapping("/admin/promotions")
    @Operation(summary = "[Admin] Create Promotion - Tạo khuyến mãi mới")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@RequestBody @Valid PromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created("Created successfully", response)
        );
    }

    @PutMapping("/admin/promotions/{id}")
    @Operation(summary = "[Admin] Modify Promotion - Chỉnh sửa khuyến mãi")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> modifyPromotion(
            @PathVariable String id,
            @RequestBody @Valid PromotionRequest request) {
        PromotionResponse response = promotionService.modifyPromotion(id, request);
        return ResponseEntity.ok(ApiResponse.success("Modified successfully", response));
    }

    @PatchMapping("/admin/promotions/{id}/disable")
    @Operation(summary = "[Admin] Disable Promotion - Vô hiệu hóa khuyến mãi")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disablePromotion(@PathVariable String id) {
        promotionService.disable(id);
        return ResponseEntity.ok(ApiResponse.noContent("Disabled successfully"));
    }

    @GetMapping("/admin/promotions/{id}")
    @Operation(summary = "[Admin] See Promotion Detail - Xem chi tiết")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> getDetails(@PathVariable String id) {
        PromotionResponse response = promotionService.getDetails(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admin/promotions")
    @Operation(summary = "[Admin] Get All - Xem danh sách tất cả khuyến mãi")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllPromotions() {
        List<PromotionResponse> response = promotionService.getAllPromotions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // ACTOR: CUSTOMER (Path: /api/v1/promotions)
    // ==========================================

    @GetMapping("/promotions")
    @Operation(summary = "[Public] Get All Active Promotions - Xem tất cả khuyến mãi đang hoạt động")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAllActivePromotions() {
        List<PromotionResponse> response = promotionService.getAllActivePromotions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/promotions/available")
    @Operation(summary = "[Customer] Check & Get Available - Lấy DS khuyến mãi hợp lệ")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> checkAndGetAvailablePromotions(
            @RequestParam(defaultValue = "0") Double orderTotal) {
        List<PromotionResponse> response = promotionService.checkAndGetAvailablePromotions(orderTotal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/promotions/calculate")
    @Operation(summary = "[Customer] Apply Promotion - Tính toán tiền giảm giá")
    public ResponseEntity<ApiResponse<Double>> calculateDiscount(
            @RequestParam String promotionId,
            @RequestParam Double orderTotal) {
        Double discountAmount = promotionService.calculateDiscount(promotionId, orderTotal);
        return ResponseEntity.ok(ApiResponse.success(discountAmount));
    }
}