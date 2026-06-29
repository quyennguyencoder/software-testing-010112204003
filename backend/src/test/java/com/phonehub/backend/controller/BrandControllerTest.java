package com.phonehub.backend.controller;

import com.phonehub.backend.dto.response.brand.BrandResponse;
import com.phonehub.backend.service.intf.IBrandService;
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

public class BrandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IBrandService brandService;

    @InjectMocks
    private BrandController brandController;
 
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(brandController).build();
    }
    // ====================================================================================
    // TEST: getAllBrands
    // ====================================================================================
    @Test
    public void getAllBrands_ShouldReturnBrandList() throws Exception {
        BrandResponse apple = BrandResponse.builder().id(1L).name("Apple").description("Apple Inc.").logoUrl("apple.png").build();
        BrandResponse samsung = BrandResponse.builder().id(2L).name("Samsung").description("Samsung Electronics").logoUrl("samsung.png").build();
        List<BrandResponse> brands = Arrays.asList(apple, samsung);

        when(brandService.getAllBrands()).thenReturn(brands);

        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách thương hiệu thành công"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Apple"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("Samsung"));
    }
    // ====================================================================================
    // TEST: getBrandById
    // ====================================================================================
    @Test
    public void getBrandById_ShouldReturnBrand() throws Exception {
        Long brandId = 1L;
        BrandResponse apple = BrandResponse.builder().id(brandId).name("Apple").description("Apple Inc.").logoUrl("apple.png").build();

        when(brandService.getBrandById(brandId)).thenReturn(apple);

        mockMvc.perform(get("/api/v1/brands/{id}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy thông tin thương hiệu thành công"))
                .andExpect(jsonPath("$.data.id").value(brandId))
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }
}
