package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.address.AddressRequest;
import com.utephonehub.backend.dto.response.address.AddressResponse;
import com.utephonehub.backend.service.IAddressService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Address", description = "API quản lý địa chỉ giao hàng")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final IAddressService addressService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ", description = "Trả về tất cả địa chỉ giao hàng của người dùng")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(HttpServletRequest request) {
        log.info("Get addresses request");
        Long userId = securityUtils.getCurrentUserId(request);
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới", description = "Tạo một địa chỉ giao hàng mới")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @Valid @RequestBody AddressRequest request,
            HttpServletRequest httpRequest) {
        log.info("Create address request");
        Long userId = securityUtils.getCurrentUserId(httpRequest);
        AddressResponse address = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm địa chỉ thành công", address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật địa chỉ", description = "Cập nhật thông tin địa chỉ giao hàng")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            HttpServletRequest httpRequest) {
        log.info("Update address request for id: {}", id);
        Long userId = securityUtils.getCurrentUserId(httpRequest);
        AddressResponse address = addressService.updateAddress(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ", description = "Xóa một địa chỉ giao hàng")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("Delete address request for id: {}", id);
        Long userId = securityUtils.getCurrentUserId(request);
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
    }

    @PutMapping("/{id}/set-default")
    @Operation(summary = "Đặt làm địa chỉ mặc định", description = "Đặt một địa chỉ làm mặc định")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("Set default address request for id: {}", id);
        Long userId = securityUtils.getCurrentUserId(request);
        AddressResponse address = addressService.setDefaultAddress(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công", address));
    }
}

