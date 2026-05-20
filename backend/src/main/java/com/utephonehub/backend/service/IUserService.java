package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.user.ChangePasswordRequest;
import com.utephonehub.backend.dto.request.user.CreateUserRequest;
import com.utephonehub.backend.dto.request.user.UpdateProfileRequest;
import com.utephonehub.backend.dto.response.user.PagedUserResponse;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.enums.UserStatus;

/**
 * Interface for User Service operations
 */
public interface IUserService {
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return UserResponse
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Update user profile
     * @param userId User ID
     * @param request Update profile request
     * @return Updated UserResponse
     */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * Change user password
     * @param userId User ID
     * @param request Change password request
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * Get all users with pagination, filtering and searching
     * @param page Page number (0-based)
     * @param size Page size
     * @param role Filter by role (null for all)
     * @param status Filter by status (null for all)
     * @param search Search keyword (username, email, fullName)
     * @return PagedUserResponse
     */
    PagedUserResponse getAllUsers(int page, int size, UserRole role, UserStatus status, String search);
    
    /**
     * Lock user account (Only CUSTOMER can be locked, ADMIN cannot)
     * @param userId User ID to lock
     * @return Updated UserResponse
     * @throws BadRequestException if trying to lock ADMIN account
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse lockUser(Long userId);
    
    /**
     * Unlock user account
     * @param userId User ID to unlock
     * @return Updated UserResponse
     * @throws BadRequestException if account is already active
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse unlockUser(Long userId);
    
    /**
     * Create new user account (CUSTOMER or ADMIN)
     * @param request Create user request with email, password, fullName, phoneNumber, role
     * @return Created UserResponse
     * @throws BadRequestException if email already exists or validation fails
     */
    UserResponse createUser(CreateUserRequest request);
}
