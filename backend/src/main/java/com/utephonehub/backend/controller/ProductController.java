package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.product.CreateProductRequest;
import com.utephonehub.backend.dto.request.product.ManageImagesRequest;
import com.utephonehub.backend.dto.request.product.UpdateProductRequest;
import com.utephonehub.backend.dto.response.product.ProductDetailResponse;
import com.utephonehub.backend.dto.response.product.ProductImageResponse;
import com.utephonehub.backend.dto.response.product.ProductListResponse;
import com.utephonehub.backend.dto.response.product.ProductTemplateResponse;
import com.utephonehub.backend.service.IProductService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Product Management
 * Handles CRUD operations, search, filtering, and stock management for products
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "API quản lý sản phẩm - CRUD, tìm kiếm, lọc và quản lý tồn kho")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

        private final IProductService productService;
        private final SecurityUtils securityUtils;
        private final com.utephonehub.backend.validator.ProductFilterValidator productFilterValidator;

        // Get product metadata greater than price
        @GetMapping("/metadata/greater-than-price")
        @Operation(summary = "Lấy metadata sản phẩm lớn hơn giá", description = "Lấy danh sách metadata sản phẩm có giá lớn hơn giá trị được chỉ định")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy metadata sản phẩm thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
        })
        public ResponseEntity<ApiResponse<List<ProductTemplateResponse>>> getProductMetadataGreaterThanPrice(
                        @Parameter(description = "Giá trị so sánh") @RequestParam BigDecimal price) {
                log.info("GET /api/v1/products/metadata/greater-than-price?price={}", price);

                List<ProductTemplateResponse> productMetadata = productService
                                .getProductMetadataGreaterThanPrice(price);

                return ResponseEntity.ok(ApiResponse.success("Lấy metadata sản phẩm thành công", productMetadata));
        }

        /**
         * ADMIN ENDPOINTS - Require ADMIN role
         */

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Tạo sản phẩm mới (Admin)", description = "Tạo một sản phẩm mới trong hệ thống")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
        })
        public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
                        @Valid @RequestBody CreateProductRequest request,
                        HttpServletRequest httpRequest) {
                log.info("POST /api/v1/products - Creating product: {}", request.getName());

                Long userId = securityUtils.getCurrentUserId(httpRequest);
                ProductDetailResponse product = productService.createProduct(request, userId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.created("Tạo sản phẩm thành công", product));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy chi tiết sản phẩm (Admin)", description = "Lấy thông tin chi tiết của một sản phẩm theo ID")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy chi tiết sản phẩm thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
        })
        public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id) {
                log.info("GET /api/v1/admin/products/{}", id);

                ProductDetailResponse product = productService.getProductById(id);

                return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm thành công", product));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Cập nhật sản phẩm (Admin)", description = "Cập nhật thông tin sản phẩm đã tồn tại")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
        })
        public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
                        @Valid @RequestBody UpdateProductRequest request,
                        HttpServletRequest httpRequest) {
                log.info("PUT /api/v1/products/{}", id);

                Long userId = securityUtils.getCurrentUserId(httpRequest);
                ProductDetailResponse product = productService.updateProduct(id, request, userId);

                return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", product));
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa sản phẩm (Admin - Soft Delete)", description = "Xóa mềm sản phẩm, có thể khôi phục sau này")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
                        HttpServletRequest httpRequest) {
                log.info("DELETE /api/v1/products/{}", id);

                Long userId = securityUtils.getCurrentUserId(httpRequest);
                productService.deleteProduct(id, userId);

                return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công", null));
        }

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xem danh sách tất cả sản phẩm (Admin)", description = "Lấy danh sách sản phẩm với tùy chọn lọc, tìm kiếm và sắp xếp. "
                        +
                        "Nếu không truyền tham số gì thì mặc định trả về tất cả sản phẩm đang hoạt động.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
        })
        public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
                        @Parameter(description = "Từ khóa tìm kiếm (tên, SKU, mô tả)") @RequestParam(required = false) String keyword,

                        @Parameter(description = "Lọc theo category ID") @RequestParam(required = false) Long categoryId,

                        @Parameter(description = "Lọc theo brand ID") @RequestParam(required = false) Long brandId,

                        @Parameter(description = "Giá tối thiểu") @RequestParam(required = false) Double minPrice,

                        @Parameter(description = "Giá tối đa") @RequestParam(required = false) Double maxPrice,

                        @Parameter(description = "Sắp xếp theo (name, price, stock, createdAt)") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,

                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(required = false, defaultValue = "desc") String sortDirection,

                        @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Số items/trang") @RequestParam(defaultValue = "20") int size) {
                log.info("GET /api/v1/admin/products - keyword: {}, categoryId: {}, brandId: {}, priceRange: [{}-{}], sort: {}({})",
                                keyword, categoryId, brandId, minPrice, maxPrice, sortBy, sortDirection);

                // Validate all filter parameters
                productFilterValidator.validateAll(keyword, minPrice, maxPrice, sortBy, sortDirection);

                // Create Pageable with sorting (only for DB-supported fields)
                Pageable pageable = createPageableWithSort(page, size, sortBy, sortDirection);

                // Get active products only (isDeleted=false)
                Page<ProductListResponse> products = productService.getProducts(
                                keyword, categoryId, brandId, minPrice, maxPrice, null,
                                false, sortBy, sortDirection, pageable);

                return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm thành công", products));
        }

        @GetMapping("/deleted")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy danh sách sản phẩm đã xóa mềm (Admin)", description = "Lấy tất cả sản phẩm đã bị xóa mềm (isDeleted=true)")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
        })
        public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getDeletedProducts(
                        @Parameter(description = "Từ khóa tìm kiếm (tên, SKU, mô tả)") @RequestParam(required = false) String keyword,

                        @Parameter(description = "Lọc theo category ID") @RequestParam(required = false) Long categoryId,

                        @Parameter(description = "Lọc theo brand ID") @RequestParam(required = false) Long brandId,

                        @Parameter(description = "Sắp xếp theo (name, price, stock, createdAt)") @RequestParam(required = false, defaultValue = "deletedAt") String sortBy,

                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(required = false, defaultValue = "desc") String sortDirection,

                        @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Số items/trang") @RequestParam(defaultValue = "20") int size) {
                log.info("GET /api/v1/admin/products/deleted - keyword: {}, categoryId: {}, brandId: {}, sort: {}({})",
                                keyword, categoryId, brandId, sortBy, sortDirection);

                // Create Pageable with sorting
                Pageable pageable = createPageable(page, size, sortBy, sortDirection);

                // Get deleted products only
                Page<ProductListResponse> products = productService.getDeletedProducts(
                                keyword, categoryId, brandId, pageable);

                return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sản phẩm đã xóa thành công", products));
        }

        @PostMapping("/{id}/restore")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Khôi phục sản phẩm đã xóa (Admin)", description = "Khôi phục sản phẩm đã bị xóa mềm")
        public ResponseEntity<ApiResponse<Void>> restoreProduct(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
                        HttpServletRequest httpRequest) {
                log.info("POST /api/v1/products/{}/restore", id);

                Long userId = securityUtils.getCurrentUserId(httpRequest);
                productService.restoreProduct(id, userId);

                return ResponseEntity.ok(ApiResponse.success("Khôi phục sản phẩm thành công", null));
        }

        @PostMapping("/{id}/images")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Quản lý hình ảnh sản phẩm (Admin)", description = "Thêm, cập nhật hoặc sắp xếp lại hình ảnh sản phẩm. Sẽ thay thế toàn bộ ảnh hiện tại.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật hình ảnh thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
        })
        public ResponseEntity<ApiResponse<Void>> manageProductImages(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
                        @Valid @RequestBody ManageImagesRequest request) {
                log.info("POST /api/v1/products/{}/images - Managing {} images", id, request.getImages().size());

                productService.manageProductImages(id, request);

                return ResponseEntity.ok(ApiResponse.success("Cập nhật hình ảnh sản phẩm thành công", null));
        }

        @DeleteMapping("/{id}/images/{imageId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa hình ảnh sản phẩm (Admin)", description = "Xóa một hình ảnh cụ thể của sản phẩm")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa hình ảnh thành công"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể xóa ảnh cuối cùng"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc hình ảnh")
        })
        public ResponseEntity<ApiResponse<Void>> deleteProductImage(
                        @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
                        @Parameter(description = "ID của hình ảnh") @PathVariable Long imageId) {
                log.info("DELETE /api/v1/admin/products/delete-image/{}/{}", id, imageId);

                productService.deleteProductImage(id, imageId);

                return ResponseEntity.ok(ApiResponse.success("Xóa hình ảnh thành công", null));
        }

        // ========== PRIVATE HELPER METHODS ==========

        /**
         * Create Pageable with Sort for DB-supported fields (name, createdAt)
         * Fields like price and stockQuantity are sorted in-memory at service layer
         */
        private Pageable createPageableWithSort(int page, int size, String sortBy, String sortDirection) {
                Sort sort = null;
                if ("name".equals(sortBy) || "createdAt".equals(sortBy)) {
                        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                                        ? Sort.Direction.ASC
                                        : Sort.Direction.DESC;
                        sort = Sort.by(direction, sortBy);
                }

                return sort != null
                                ? PageRequest.of(page, size, sort)
                                : PageRequest.of(page, size);
        }

        /**
         * Create Pageable with Sort (for all fields)
         */
        private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
                Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Sort sort = Sort.by(direction, sortBy);
                return PageRequest.of(page, size, sort);
        }

}
