package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.response.brand.BrandResponse;
import com.utephonehub.backend.service.IBrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brand (Public)", description = "API công khai xem thương hiệu sản phẩm")
public class BrandController {

    private final IBrandService brandService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách tất cả thương hiệu",
            description = "Trả về danh sách tất cả thương hiệu, sắp xếp theo tên. " +
                    "**API này là công khai, không cần xác thực.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        log.info("Get all brands request");
        List<BrandResponse> brands = brandService.getAllBrands();

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách thương hiệu thành công", brands)
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết thương hiệu theo ID",
            description = "Trả về thông tin chi tiết của một thương hiệu. " +
                    "**API này là công khai, không cần xác thực.**"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy thương hiệu"
            )
    })
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(
            @Parameter(description = "ID của thương hiệu", required = true)
            @PathVariable Long id
    ) {
        log.info("Get brand by id request: {}", id);
        BrandResponse brand = brandService.getBrandById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin thương hiệu thành công", brand)
        );
    }
}

