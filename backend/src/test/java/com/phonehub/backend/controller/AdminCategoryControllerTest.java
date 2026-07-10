package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.category.CreateCategoryRequest;
import com.phonehub.backend.dto.request.category.UpdateCategoryRequest;
import com.phonehub.backend.dto.response.category.CategoryResponse;
import com.phonehub.backend.service.intf.ICategoryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminCategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private AdminCategoryController adminCategoryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminCategoryController).build();
    }

    @Test
    public void createCategory_ShouldReturnCreatedCategory() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Tablets")
                .description("Tablet category")
                .parentId(null)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .name("Tablets")
                .description("Tablet category")
                .parentId(null)
                .build();

        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201)) // Response.created
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo danh mục thành công"))
                .andExpect(jsonPath("$.data.name").value("Tablets"));
    }

    @Test
    public void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        Long id = 1L;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Tablets & iPads")
                .description("Tablet and iPad category")
                .parentId(null)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(id)
                .name("Tablets & iPads")
                .description("Tablet and iPad category")
                .parentId(null)
                .build();

        when(categoryService.updateCategory(eq(id), any(UpdateCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật danh mục thành công"))
                .andExpect(jsonPath("$.data.name").value("Tablets & iPads"));
    }

    @Test
    public void deleteCategory_ShouldReturnSuccess() throws Exception {
        Long id = 1L;
        doNothing().when(categoryService).deleteCategory(id);

        mockMvc.perform(delete("/api/v1/admin/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa danh mục thành công"));
    }
}
