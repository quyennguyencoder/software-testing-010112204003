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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category (Public)", description = "API công khai xem danh mục sản phẩm")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách danh mục theo parentId",
            description = "Trả về danh sách danh mục. " +
                    "Nếu parentId=null hoặc không truyền: trả về danh mục gốc. " +
                    "Nếu parentId=<id>: trả về danh mục con của parent đó. " +
                    "**API này là công khai, không cần xác thực.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy danh mục cha (nếu parentId được cung cấp)"
            )
    })
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @Parameter(description = "ID của danh mục cha (null để lấy danh mục gốc)", required = false)
            @RequestParam(required = false) Long parentId
    ) {
        log.info("Get categories request with parentId: {}", parentId);
        List<CategoryResponse> categories = categoryService.getCategoriesByParentId(parentId);

        String message = parentId == null
                ? "Lấy danh sách danh mục gốc thành công"
                : "Lấy danh sách danh mục con thành công";

        return ResponseEntity.ok(
                ApiResponse.success(message, categories)
        );
    }
}

