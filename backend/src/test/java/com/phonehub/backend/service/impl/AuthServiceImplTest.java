package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.auth.ForgotPasswordRequest;
import com.phonehub.backend.dto.request.auth.LoginRequest;
import com.phonehub.backend.dto.request.auth.RefreshTokenRequest;
import com.phonehub.backend.dto.request.auth.RegisterRequest;
import com.phonehub.backend.dto.request.auth.VerifyOtpRequest;
import com.phonehub.backend.dto.response.auth.AuthResponse;
import com.phonehub.backend.dto.response.user.UserResponse;
import com.phonehub.backend.entity.Cart;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.UserRole;
import com.phonehub.backend.enums.UserStatus;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ConflictException;
import com.phonehub.backend.exception.UnauthorizedException;
import com.phonehub.backend.mapper.UserMapper;
import com.phonehub.backend.repository.CartRepository;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.service.intf.IEmailService;
import com.phonehub.backend.util.JwtTokenProvider;
import com.phonehub.backend.util.OtpGenerator;
import com.phonehub.backend.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OtpGenerator otpGenerator;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private IEmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserResponse testUserResponse;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPass")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setEmail("test@example.com");

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("New User")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());
        doNothing().when(emailService).sendRegistrationEmail(testUser.getEmail(), testUser.getFullName());
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(emailService, times(1)).sendRegistrationEmail(anyString(), any());
    }

    @Test
    void register_PasswordMismatch_ThrowsException() {
        registerRequest.setConfirmPassword("differentPass");

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "hashedPass")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com")).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(1L, "test@example.com")).thenReturn("refresh_token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(eq("refresh_token:1"), eq("refresh_token"), eq(7L), eq(TimeUnit.DAYS));
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
    }

    @Test
    void login_LockedUser_ThrowsException() {
        testUser.setStatus(UserStatus.LOCKED);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid_refresh_token");

        when(jwtTokenProvider.validateToken("valid_refresh_token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid_refresh_token")).thenReturn(1L);
        when(jwtTokenProvider.getEmailFromToken("valid_refresh_token")).thenReturn("test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refresh_token:1")).thenReturn("valid_refresh_token");
        when(jwtTokenProvider.generateAccessToken(1L, "test@example.com")).thenReturn("new_access_token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
    }

    @Test
    void logout_Success() {
        authService.logout(1L);
        verify(redisTemplate, times(1)).delete("refresh_token:1");
    }

    @Test
    void requestPasswordReset_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpGenerator.generateOtp()).thenReturn("123456");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(eq("otp:test@example.com"), eq("123456"), eq(5L), eq(TimeUnit.MINUTES));
        doNothing().when(emailService).sendOtpEmail("test@example.com", "123456");

        authService.requestPasswordReset(request);

        verify(emailService, times(1)).sendOtpEmail("test@example.com", "123456");
    }

    @Test
    void verifyOtpAndResetPassword_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123!");
        request.setConfirmPassword("newPass123!");

        when(passwordEncoder.isValidPassword("newPass123!")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:test@example.com")).thenReturn("123456");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPass123!")).thenReturn("newHashedPass");

        authService.verifyOtpAndResetPassword(request);

        verify(userRepository, times(1)).save(testUser);
        verify(redisTemplate, times(1)).delete("otp:test@example.com");
    }
}
