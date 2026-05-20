package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.PromotionTemplateRequest;
import com.utephonehub.backend.dto.response.PromotionTemplateResponse;
import com.utephonehub.backend.service.IPromotionTemplateService;
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
@RequestMapping("/api/v1/admin/promotion-templates")
@RequiredArgsConstructor
@Tag(name = "Promotion Template Controller", description = "Quản lý Template cho Promotion")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class PromotionTemplateController {

    private final IPromotionTemplateService templateService;

    @PostMapping
    @Operation(summary = "[Admin] Create Template - Tạo template mới")
    public ResponseEntity<ApiResponse<PromotionTemplateResponse>> createTemplate(
            @RequestBody @Valid PromotionTemplateRequest request) {
        PromotionTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Template created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "[Admin] Update Template - Cập nhật template")
    public ResponseEntity<ApiResponse<PromotionTemplateResponse>> updateTemplate(
            @PathVariable String id,
            @RequestBody @Valid PromotionTemplateRequest request) {
        PromotionTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Template updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[Admin] Delete Template - Xóa template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.noContent("Template deleted successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "[Admin] Get Template Detail - Xem chi tiết template")
    public ResponseEntity<ApiResponse<PromotionTemplateResponse>> getTemplateById(@PathVariable String id) {
        PromotionTemplateResponse response = templateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "[Admin] Get All Templates - Lấy danh sách tất cả templates")
    public ResponseEntity<ApiResponse<List<PromotionTemplateResponse>>> getAllTemplates() {
        List<PromotionTemplateResponse> response = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
