package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.phonehub.backend.dto.request.user.ChangePasswordRequest;
import com.phonehub.backend.dto.request.user.UpdateProfileRequest;
import com.phonehub.backend.dto.response.user.UserResponse;
import com.phonehub.backend.service.intf.IUserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserService userService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void getCurrentUser_ShouldReturnUserResponse() throws Exception {
        Long userId = 1L;
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setFullName("John Doe");

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    public void updateProfile_ShouldReturnUpdatedUserResponse() throws Exception {
        Long userId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        request.setPhoneNumber("0987654321");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setFullName("Updated Name");
        userResponse.setPhoneNumber("0987654321");

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/user/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật thông tin thành công"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0987654321"));
    }

    @Test
    public void changePassword_ShouldReturnSuccess() throws Exception {
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass123!");
        request.setNewPassword("NewPass123!");
        request.setConfirmPassword("NewPass123!");

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        doNothing().when(userService).changePassword(eq(userId), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/v1/user/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));
    }
}
