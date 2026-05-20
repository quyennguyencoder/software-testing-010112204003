package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.user.CreateUserRequest;
import com.utephonehub.backend.dto.response.user.PagedUserResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;
import com.utephonehub.backend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - User Management", description = "API quản lý người dùng dành cho Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IUserService userService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách người dùng",
            description = "Lấy danh sách tất cả người dùng với phân trang, lọc theo role/status và tìm kiếm theo username/email/fullName"
    )
    public ResponseEntity<ApiResponse<PagedUserResponse>> getAllUsers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Lọc theo vai trò (ADMIN, CUSTOMER, hoặc để trống để lấy tất cả)")
            @RequestParam(required = false) UserRole role,

            @Parameter(description = "Lọc theo trạng thái (ACTIVE, LOCKED, hoặc để trống để lấy tất cả)")
            @RequestParam(required = false) UserStatus status,

            @Parameter(description = "Từ khóa tìm kiếm (tìm trong username, email, fullName)")
            @RequestParam(required = false) String search
    ) {
        log.info("Admin get all users - page: {}, size: {}, role: {}, status: {}, search: {}",
                page, size, role, status, search);

        PagedUserResponse response = userService.getAllUsers(page, size, role, status, search);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách người dùng thành công",
                response
        ));
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Lấy chi tiết người dùng",
            description = "Lấy thông tin chi tiết của một người dùng cụ thể theo ID"
    )
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID của người dùng", example = "1", required = true)
            @PathVariable Long userId
    ) {
        log.info("Admin get user detail - userId: {}", userId);

        UserResponse user = userService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin người dùng thành công",
                user
        ));
    }

    @PutMapping("/{userId}/lock")
    @Operation(
            summary = "Khóa tài khoản người dùng",
            description = "Khóa tài khoản CUSTOMER. Không thể khóa tài khoản ADMIN."
    )
    public ResponseEntity<ApiResponse<UserResponse>> lockUser(
            @Parameter(description = "ID của người dùng cần khóa", example = "1", required = true)
            @PathVariable Long userId
    ) {
        log.info("Admin lock user - userId: {}", userId);

        UserResponse user = userService.lockUser(userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Tài khoản đã được khóa thành công",
                user
        ));
    }

    @PutMapping("/{userId}/unlock")
    @Operation(
            summary = "Gỡ khóa tài khoản người dùng",
            description = "Mở khóa tài khoản đã bị khóa, cho phép người dùng đăng nhập trở lại."
    )
    public ResponseEntity<ApiResponse<UserResponse>> unlockUser(
            @Parameter(description = "ID của người dùng cần gỡ khóa", example = "1", required = true)
            @PathVariable Long userId
    ) {
        log.info("Admin unlock user - userId: {}", userId);

        UserResponse user = userService.unlockUser(userId);

        return ResponseEntity.ok(ApiResponse.success(
                "Tài khoản đã được mở khóa thành công",
                user
        ));
    }

    @PostMapping
    @Operation(
            summary = "Tạo tài khoản mới",
            description = "Tạo tài khoản CUSTOMER hoặc ADMIN mới. Email phải unique, password tối thiểu 8 ký tự (có chữ hoa, chữ thường, số)."
    )
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        log.info("Admin create new user - email: {}, role: {}", request.getEmail(), request.getRole());

        UserResponse user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "Tài khoản đã được tạo thành công",
                        user
                ));
    }
}
