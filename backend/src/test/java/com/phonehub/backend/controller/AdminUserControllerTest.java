package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.user.CreateUserRequest;
import com.phonehub.backend.dto.response.user.PagedUserResponse;
import com.phonehub.backend.dto.response.user.UserResponse;
import com.phonehub.backend.enums.UserRole;
import com.phonehub.backend.enums.UserStatus;
import com.phonehub.backend.service.intf.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController).build();
    }

    @Test
    public void getAllUsers_ShouldReturnPagedUsers() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("testuser");

        PagedUserResponse pagedResponse = PagedUserResponse.builder()
                .content(Collections.singletonList(user))
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .pageSize(10)
                .build();

        when(userService.getAllUsers(0, 10, null, null, null)).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    public void getUserById_ShouldReturnUser() throws Exception {
        Long userId = 1L;
        UserResponse user = new UserResponse();
        user.setId(userId);
        user.setUsername("testuser");

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/v1/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId));
    }

    @Test
    public void lockUser_ShouldReturnLockedUser() throws Exception {
        Long userId = 1L;
        UserResponse user = new UserResponse();
        user.setId(userId);
        user.setStatus(UserStatus.LOCKED.name());

        when(userService.lockUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{userId}/lock", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tài khoản đã được khóa thành công"));
    }

    @Test
    public void unlockUser_ShouldReturnUnlockedUser() throws Exception {
        Long userId = 1L;
        UserResponse user = new UserResponse();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE.name());

        when(userService.unlockUser(userId)).thenReturn(user);

        mockMvc.perform(put("/api/v1/admin/users/{userId}/unlock", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tài khoản đã được mở khóa thành công"));
    }

    @Test
    public void createUser_ShouldReturnCreatedUser() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .email("admin@example.com")
                .password("Admin123!")
                .fullName("Admin User")
                .phoneNumber("0987654321")
                .role(UserRole.ADMIN)
                .build();

        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setEmail("admin@example.com");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tài khoản đã được tạo thành công"));
    }
}
