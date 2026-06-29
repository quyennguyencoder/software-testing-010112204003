package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.phonehub.backend.dto.request.auth.*;
import com.phonehub.backend.dto.response.auth.AuthResponse;
import com.phonehub.backend.dto.response.user.UserResponse;
import com.phonehub.backend.service.intf.IAuthService;
import com.phonehub.backend.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthService authService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void register_ShouldReturnCreatedUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    public void registerAdmin_ShouldReturnCreatedAdmin() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("adminuser")
                .fullName("Admin User")
                .email("admin@example.com")
                .password("password123")
                .confirmPassword("password123")
                .build();

        UserResponse userResponse = new UserResponse();
        userResponse.setId(2L);
        userResponse.setUsername("adminuser");
        userResponse.setEmail("admin@example.com");

        when(authService.registerAdmin(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng ký tài khoản Admin thành công"))
                .andExpect(jsonPath("$.data.username").value("adminuser"));
    }

    @Test
    public void login_ShouldReturnAuthResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("access_token_123")
                .refreshToken("refresh_token_123")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("access_token_123"));
    }

    @Test
    public void refreshToken_ShouldReturnAuthResponse() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh_token_123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Làm mới token thành công")) // standard check or success message
                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"));
    }

    @Test
    public void logout_ShouldReturnSuccess() throws Exception {
        Long userId = 1L;
        when(securityUtils.getUserIdIfAuthenticated(any())).thenReturn(userId);
        doNothing().when(authService).logout(userId);

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));
    }

    @Test
    public void requestPasswordReset_ShouldReturnSuccess() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().email("test@example.com").build();
        doNothing().when(authService).requestPasswordReset(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void verifyOtpAndResetPassword_ShouldReturnSuccess() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .newPassword("newpassword")
                .confirmPassword("newpassword")
                .build();

        doNothing().when(authService).verifyOtpAndResetPassword(any(VerifyOtpRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void verifyRegistrationOtp_ShouldReturnSuccess() throws Exception {
        VerifyRegistrationOtpRequest request = VerifyRegistrationOtpRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        doNothing().when(authService).verifyRegistrationOtp(any(VerifyRegistrationOtpRequest.class));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }
}
