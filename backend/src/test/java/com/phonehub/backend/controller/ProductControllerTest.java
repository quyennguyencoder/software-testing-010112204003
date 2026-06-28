package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.product.CreateProductRequest;
import com.phonehub.backend.dto.request.product.ManageImagesRequest;
import com.phonehub.backend.dto.request.product.UpdateProductRequest;
import com.phonehub.backend.dto.response.product.ProductDetailResponse;
import com.phonehub.backend.dto.response.product.ProductListResponse;
import com.phonehub.backend.dto.response.product.ProductTemplateResponse;
import com.phonehub.backend.service.intf.IProductService;
import com.phonehub.backend.util.SecurityUtils;
import com.phonehub.backend.validator.ProductFilterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IProductService productService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ProductFilterValidator productFilterValidator;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .build();
    }
    // ====================================================================================
    // TEST: getProductMetadataGreaterThanPrice
    // ====================================================================================
    // Test case 1: Thành công (200 OK)
    @Test
    public void getProductMetadataGreaterThanPrice_ValidPrice_Returns200() throws Exception {
        // Arrange
        BigDecimal testPrice = new BigDecimal("15000000");
        ProductTemplateResponse mockTemplate = new ProductTemplateResponse();
        when(productService.getProductMetadataGreaterThanPrice(testPrice))
                .thenReturn(Collections.singletonList(mockTemplate));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/products/metadata/greater-than-price")
                        .param("price", "15000000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy metadata sản phẩm thành công"))
                .andExpect(jsonPath("$.data").isArray());
    }
    // Test case 2: Lỗi thiếu tham số price không truyền price (400 Bad Request)
   @Test
    public void getProductMetadataGreaterThanPrice_MissingPrice_Returns400() throws Exception {
        // Act & Assert: Gọi API nhưng không đính kèm param "price"
        mockMvc.perform(get("/api/v1/admin/products/metadata/greater-than-price")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Test case 3: Lỗi sai kiểu dữ liệu (Type Mismatch) (400 Bad Request)
    @Test
    public void getProductMetadataGreaterThanPrice_TypeMismatch_Returns400() throws Exception {
        // Act & Assert: Truyền chữ "abc" thay vì số tiền
        mockMvc.perform(get("/api/v1/admin/products/metadata/greater-than-price")
                        .param("price", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    // Test case 4: Giá trị biên - Giá bằng 0 
    @Test
    public void getProductMetadataGreaterThanPrice_BoundaryPriceZero_Returns200() throws Exception {
        // Arrange
        BigDecimal boundaryPrice = BigDecimal.ZERO;
        ProductTemplateResponse mockTemplate = new ProductTemplateResponse();
        when(productService.getProductMetadataGreaterThanPrice(boundaryPrice))
                .thenReturn(Collections.singletonList(mockTemplate));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/products/metadata/greater-than-price")
                        .param("price", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    // ====================================================================================
    // TEST: createProduct
    // ====================================================================================
    // Test case 1: Thành công (201 Created)
    @Test
    public void createProduct_ValidRequest_Returns201() throws Exception {
        String requestJson = """
            {
              "name": "PhoneHub Test Product",
              "description": "Created by Postman collection",
              "thumbnailUrl": "https://example.com/phone.png",
              "categoryId": 1,
              "brandId": 1,
              "status": true,
              "templates": [
                {
                  "sku": "PH-12345",
                  "color": "Black",
                  "storage": "128GB",
                  "ram": "8GB",
                  "price": 12990000,
                  "stockQuantity": 20,
                  "status": true
                }
              ],
              "metadata": {
                "salePrice": 12990000,
                "screenSize": 6.7,
                "batteryCapacity": 5000,
                "operatingSystem": "Android"
              },
              "images": []
            }
            """;

        ProductDetailResponse mockResponse = new ProductDetailResponse();
        when(securityUtils.getCurrentUserId(any())).thenReturn(1L); 
        when(productService.createProduct(any(), eq(1L))).thenReturn(mockResponse);
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print()) 
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tạo sản phẩm thành công"));
    }
    // Test case 2: Lỗi thiếu trường bắt buộc (400 Bad Request)
    @Test
    public void createProduct_InvalidRequest_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) 
                .andExpect(status().isBadRequest());
    }
    // ====================================================================================
    // TEST: getProductById
    // ====================================================================================
    // Test case 1: Thành công (200 OK)
    @Test
    public void getProductById_ValidId_Returns200() throws Exception {
        Long productId = 1L;
        ProductDetailResponse mockResponse = new ProductDetailResponse();

        when(productService.getProductById(productId)).thenReturn(mockResponse);
        mockMvc.perform(get("/api/v1/admin/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy chi tiết sản phẩm thành công"));
    }
    // Test case 2: Lỗi không tìm thấy sản phẩm (404 Not Found)
    @Test
    public void getProductById_TypeMismatch_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products/{id}", "abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()); 
    }
}