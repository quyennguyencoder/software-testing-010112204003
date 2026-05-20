package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.category.CreateCategoryRequest;
import com.utephonehub.backend.dto.request.category.UpdateCategoryRequest;
import com.utephonehub.backend.dto.response.category.CategoryResponse;
import com.utephonehub.backend.service.ICategoryService;
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
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Category", description = "API quản lý danh mục sản phẩm (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Tạo danh mục mới",
            description = "Tạo một danh mục mới. " +
                    "Nếu parentId=null: tạo danh mục gốc. " +
                    "Nếu parentId=<id>: tạo danh mục con. " +
                    "Kiểm tra trùng tên trong cùng cấp parentId. " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Tạo danh mục thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc tên danh mục đã tồn tại"
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
                    description = "Không tìm thấy danh mục cha"
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        log.info("Create category request with name: {}, parentId: {}", request.getName(), request.getParentId());
        CategoryResponse category = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo danh mục thành công", category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Cập nhật danh mục",
            description = "Cập nhật thông tin của danh mục đã tồn tại. " +
                    "Kiểm tra trùng tên trong cùng cấp parentId (loại trừ chính nó). " +
                    "Không cho phép đặt danh mục làm cha của chính nó. " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật danh mục thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu không hợp lệ hoặc tên danh mục đã tồn tại"
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
                    description = "Không tìm thấy danh mục cần cập nhật hoặc danh mục cha"
            )
    })
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "ID của danh mục cần cập nhật", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        log.info("Update category request for id: {} with name: {}, parentId: {}",
                id, request.getName(), request.getParentId());
        CategoryResponse category = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật danh mục thành công", category)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[ADMIN] Xóa danh mục",
            description = "Xóa danh mục theo ID. " +
                    "Kiểm tra ràng buộc trước khi xóa: " +
                    "Không cho phép xóa nếu danh mục có danh mục con hoặc có sản phẩm liên kết. " +
                    "**Yêu cầu quyền ADMIN.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Xóa danh mục thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Không thể xóa do có danh mục con hoặc sản phẩm liên kết"
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
                    description = "Không tìm thấy danh mục cần xóa"
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "ID của danh mục cần xóa", required = true)
            @PathVariable Long id
    ) {
        log.info("Delete category request for id: {}", id);
        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success("Xóa danh mục thành công", null)
        );
    }
}

