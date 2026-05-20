package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.user.ChangePasswordRequest;
import com.utephonehub.backend.dto.request.user.UpdateProfileRequest;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.service.IUserService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "API quản lý thông tin người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final IUserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại", description = "Trả về thông tin chi tiết của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request) {
        log.info("Get current user info");
        Long userId = securityUtils.getCurrentUserId(request);
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Cập nhật họ tên và số điện thoại")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        log.info("Update profile request");
        Long userId = securityUtils.getCurrentUserId(httpRequest);
        UserResponse user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", user));
    }

    @PostMapping("/password")
    @Operation(summary = "Đổi mật khẩu", description = "Cập nhật mật khẩu hiện tại sang mật khẩu mới")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        log.info("Change password request");
        Long userId = securityUtils.getCurrentUserId(httpRequest);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }
}

