package com.phonehub.backend.controller;

import com.phonehub.backend.dto.response.category.CategoryResponse;
import com.phonehub.backend.service.intf.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    public void getCategories_WithNullParentId_ShouldReturnRootCategories() throws Exception {
        CategoryResponse root1 = CategoryResponse.builder().id(1L).name("Smartphones").parentId(null).build();
        CategoryResponse root2 = CategoryResponse.builder().id(2L).name("Accessories").parentId(null).build();
        List<CategoryResponse> categories = Arrays.asList(root1, root2);

        when(categoryService.getCategoriesByParentId(null)).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách danh mục gốc thành công"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Smartphones"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("Accessories"));
    }

    @Test
    public void getCategories_WithParentId_ShouldReturnSubCategories() throws Exception {
        Long parentId = 1L;
        CategoryResponse sub1 = CategoryResponse.builder().id(3L).name("iOS").parentId(parentId).build();
        CategoryResponse sub2 = CategoryResponse.builder().id(4L).name("Android").parentId(parentId).build();
        List<CategoryResponse> categories = Arrays.asList(sub1, sub2);

        when(categoryService.getCategoriesByParentId(parentId)).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories").param("parentId", String.valueOf(parentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách danh mục con thành công"))
                .andExpect(jsonPath("$.data[0].id").value(3L))
                .andExpect(jsonPath("$.data[0].name").value("iOS"))
                .andExpect(jsonPath("$.data[1].id").value(4L))
                .andExpect(jsonPath("$.data[1].name").value("Android"));
    }
}
