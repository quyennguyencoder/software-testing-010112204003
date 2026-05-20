package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.brand.CreateBrandRequest;
import com.utephonehub.backend.dto.request.brand.UpdateBrandRequest;
import com.utephonehub.backend.dto.response.brand.BrandResponse;
import com.utephonehub.backend.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Brand", description = "API quản lý thương hiệu sản phẩm (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminBrandController {

    private final IBrandService brandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Tạo thương hiệu mới",
            description = "Tạo một thương hiệu mới. " +
                    "Kiểm tra trùng tên. " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Tạo thương hiệu thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc tên thương hiệu đã tồn tại"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập (yêu cầu ADMIN)"
            )
    })
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody CreateBrandRequest request
    ) {
        log.info("Create brand request with name: {}", request.getName());
        BrandResponse brand = brandService.createBrand(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thương hiệu thành công", brand));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Cập nhật thương hiệu",
            description = "Cập nhật thông tin của thương hiệu đã tồn tại. " +
                    "Kiểm tra trùng tên (loại trừ chính nó). " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thương hiệu thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc tên thương hiệu đã tồn tại"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập (yêu cầu ADMIN)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy thương hiệu cần cập nhật"
            )
    })
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @Parameter(description = "ID của thương hiệu cần cập nhật", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request
    ) {
        log.info("Update brand request for id: {} with name: {}", id, request.getName());
        BrandResponse brand = brandService.updateBrand(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật thương hiệu thành công", brand)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Xóa thương hiệu",
            description = "Xóa thương hiệu theo ID. " +
                    "Kiểm tra ràng buộc trước khi xóa: " +
                    "Không cho phép xóa nếu thương hiệu có sản phẩm liên kết. " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xóa thương hiệu thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Không thể xóa do có sản phẩm liên kết"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Chưa xác thực"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập (yêu cầu ADMIN)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy thương hiệu cần xóa"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteBrand(
            @Parameter(description = "ID của thương hiệu cần xóa", required = true)
            @PathVariable Long id
    ) {
        log.info("Delete brand request for id: {}", id);
        brandService.deleteBrand(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa thương hiệu thành công", null)
        );
    }
}

