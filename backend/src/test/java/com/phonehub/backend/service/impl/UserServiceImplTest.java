package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.user.ChangePasswordRequest;
import com.phonehub.backend.dto.request.user.CreateUserRequest;
import com.phonehub.backend.dto.request.user.UpdateProfileRequest;
import com.phonehub.backend.dto.response.user.PagedUserResponse;
import com.phonehub.backend.dto.response.user.UserResponse;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.UserRole;
import com.phonehub.backend.enums.UserStatus;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.exception.UnauthorizedException;
import com.phonehub.backend.mapper.UserMapper;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("test")
                .fullName("Test User")
                .passwordHash("hashedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setEmail("test@example.com");
        testUserResponse.setFullName("Test User");
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setPhoneNumber("0987654321");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setFullName("Updated Name");
        when(userMapper.toResponse(testUser)).thenReturn(updatedResponse);

        UserResponse response = userService.updateProfile(1L, request);

        assertNotNull(response);
        assertEquals("Updated Name", response.getFullName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass123!");
        request.setNewPassword("newPass123!");
        request.setConfirmPassword("newPass123!");

        when(passwordEncoder.isValidPassword("newPass123!")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass123!", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123!")).thenReturn("newHashedPassword");

        userService.changePassword(1L, request);

        assertEquals("newHashedPassword", testUser.getPasswordHash());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void changePassword_Mismatch_ThrowsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass123!");
        request.setConfirmPassword("wrongMatch!");

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> userService.changePassword(1L, request));

        assertEquals("Mật khẩu và xác nhận mật khẩu không khớp", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void lockUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserResponse lockedResponse = new UserResponse();
        lockedResponse.setStatus(UserStatus.LOCKED.name());
        when(userMapper.toResponse(testUser)).thenReturn(lockedResponse);

        UserResponse response = userService.lockUser(1L);

        assertEquals(UserStatus.LOCKED.name(), response.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void lockUser_AdminAccount_ThrowsException() {
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.lockUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unlockUser_Success() {
        testUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserResponse unlockedResponse = new UserResponse();
        unlockedResponse.setStatus(UserStatus.ACTIVE.name());
        when(userMapper.toResponse(testUser)).thenReturn(unlockedResponse);

        UserResponse response = userService.unlockUser(1L);

        assertEquals(UserStatus.ACTIVE.name(), response.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void createUser_Success() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("new@example.com")
                .password("Password123!")
                .fullName("New User")
                .role(UserRole.CUSTOMER)
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.isValidPassword("Password123!")).thenReturn(true);
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllUsers_Success() {
        Page<User> page = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        PagedUserResponse response = userService.getAllUsers(0, 10, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }
}
