package com.phonehub.backend.controller;

import com.phonehub.backend.dto.response.brand.BrandResponse;
import com.phonehub.backend.service.intf.IBrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AdminBrandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IBrandService brandService;

    @InjectMocks
    private AdminBrandController adminBrandController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminBrandController).build();
    }
    // ====================================================================================
    // TEST: createBrand 
    // ====================================================================================
    // Test case 1: Tạo thương hiệu thành công 
    @Test
    public void createBrand_ValidRequest_Returns201() throws Exception {
        BrandResponse mockResponse = BrandResponse.builder()
                .id(1L)
                .name("Apple")
                .build();

        when(brandService.createBrand(any())).thenReturn(mockResponse);
        String requestJson = """
            {
              "name": "Apple",
              "description": "Thương hiệu Apple",
              "logoUrl": "https://example.com/apple.png"
            }
            """;
        mockMvc.perform(post("/api/v1/admin/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo thương hiệu thành công"))
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }
    // Test case 2: Không gửi Request Body -> Báo lỗi 400 Bad Request
    @Test
    public void createBrand_MissingBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/brands")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    // ====================================================================================
    // TEST: updateBrand
    // ====================================================================================
    // Test case 1: Cập nhật thương hiệu thành công
    @Test
    public void updateBrand_ValidRequest_Returns200() throws Exception {
        Long brandId = 1L;
        
        BrandResponse mockResponse = BrandResponse.builder()
                .id(brandId)
                .name("Apple Updated")
                .description("Đã cập nhật")
                .logoUrl("https://example.com/apple_new.png")
                .build();

        when(brandService.updateBrand(eq(brandId), any())).thenReturn(mockResponse);
        String requestJson = """
            {
              "name": "Apple Updated",
              "description": "Đã cập nhật",
              "logoUrl": "https://example.com/apple_new.png"
            }
            """;
        mockMvc.perform(put("/api/v1/admin/brands/{id}", brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật thương hiệu thành công"))
                .andExpect(jsonPath("$.data.name").value("Apple Updated"));
    }
    // Test case 2: ID thương hiệu sai định dạng chữ 
    @Test
    public void updateBrand_IdTypeMismatch_Returns400() throws Exception {
        String requestJson = """
            {
              "name": "Apple Updated",
              "logoUrl": "https://example.com/apple_new.png"
            }
            """;

        // Cố tình truyền "abc" vào vị trí của ID
        mockMvc.perform(put("/api/v1/admin/brands/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
    // Test case 3: Không gửi Request Body 
    @Test
    public void updateBrand_MissingBody_Returns400() throws Exception {
        Long brandId = 1L;
        
        // Cố tình gọi PUT nhưng không đính kèm .content()
        mockMvc.perform(put("/api/v1/admin/brands/{id}", brandId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    // ====================================================================================
    // TEST: deleteBrand 
    // ====================================================================================
    // Test case 1: Xóa thương hiệu thành công
    @Test
    public void deleteBrand_ValidId_Returns200() throws Exception {
        Long brandId = 1L;
        doNothing().when(brandService).deleteBrand(brandId);
        
        mockMvc.perform(delete("/api/v1/admin/brands/{id}", brandId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa thương hiệu thành công"));
    }
    // Test case 2: ID sai định dạng chữ 
    @Test
    public void deleteBrand_IdTypeMismatch_Returns400() throws Exception {
        // Cố tình truyền "abc" vào vị trí của ID
        mockMvc.perform(delete("/api/v1/admin/brands/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}