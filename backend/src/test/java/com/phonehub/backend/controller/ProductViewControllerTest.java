package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.productview.ProductFilterRequest;
import com.phonehub.backend.dto.request.productview.ProductSearchFilterRequest;
import com.phonehub.backend.dto.response.productview.CategoryProductsResponse;
import com.phonehub.backend.dto.response.productview.ProductCardResponse;
import com.phonehub.backend.dto.response.productview.ProductComparisonResponse;
import com.phonehub.backend.dto.response.productview.ProductDetailViewResponse;
import com.phonehub.backend.service.intf.IProductViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IProductViewService productViewService;

    @InjectMocks
    private ProductViewController productViewController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productViewController)
                .setMessageConverters(TestPageSerializer.createPageMessageConverter())
                .build();
    }

    @Test
    public void searchProducts_ShouldReturnPagedProducts() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);
        productCard.setName("iPhone 15");

        Page<ProductCardResponse> pagedResponse = new PageImpl<>(Collections.singletonList(productCard));

        when(productViewService.searchAndFilterProducts(any(ProductSearchFilterRequest.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/products/search")
                .param("keyword", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("iPhone 15"));
    }

    @Test
    public void filterProducts_ShouldReturnFilteredProducts() throws Exception {
        ProductFilterRequest request = new ProductFilterRequest();
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);
        productCard.setName("iPhone 15");

        Page<ProductCardResponse> pagedResponse = new PageImpl<>(Collections.singletonList(productCard));

        when(productViewService.filterProducts(any(ProductFilterRequest.class))).thenReturn(pagedResponse);

        mockMvc.perform(post("/api/v1/products/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getProductDetail_ShouldReturnProductDetail() throws Exception {
        Long id = 1L;
        ProductDetailViewResponse detailResponse = new ProductDetailViewResponse();
        detailResponse.setId(id);
        detailResponse.setName("iPhone 15");

        when(productViewService.getProductDetailById(id)).thenReturn(detailResponse);

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("iPhone 15"));
    }

    @Test
    public void getProductsByCategory_ShouldReturnCategoryProducts() throws Exception {
        Long categoryId = 1L;
        CategoryProductsResponse categoryResponse = new CategoryProductsResponse();

        when(productViewService.getProductsByCategory(eq(categoryId), any(ProductSearchFilterRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/products/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void compareProducts_ShouldReturnComparison() throws Exception {
        ProductComparisonResponse comparisonResponse = new ProductComparisonResponse();

        when(productViewService.compareProducts(any())).thenReturn(comparisonResponse);

        mockMvc.perform(post("/api/v1/products/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[1, 2]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getRelatedProducts_ShouldReturnRelatedProducts() throws Exception {
        Long id = 1L;
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(2L);
        productCard.setName("iPhone 14");

        when(productViewService.getRelatedProducts(id, 5)).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/{id}/related", id).param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(2L));
    }

    @Test
    public void getBestSellingProducts_ShouldReturnBestSellingProducts() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.getBestSellingProducts(5)).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/best-selling").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getNewArrivals_ShouldReturnNewArrivals() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.getNewArrivals(5)).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/new-arrivals").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getFeaturedProducts_ShouldReturnFeaturedProducts() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.getFeaturedProducts(5)).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/featured").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getProductsOnSale_ShouldReturnProductsOnSale() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.getProductsOnSale(5)).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/on-sale").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void filterByRam_ShouldReturnFiltered() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.filterByRamWithLimit(any(), any(), eq(5))).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/filter/ram")
                .param("ramOptions", "8GB")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void filterByStorage_ShouldReturnFiltered() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.filterByStorageWithLimit(any(), any(), eq(5))).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/filter/storage")
                .param("storageOptions", "128GB")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void filterByBattery_ShouldReturnFiltered() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.filterByBatteryWithLimit(any(), any(), any(), eq(5))).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/filter/battery")
                .param("minBattery", "4000")
                .param("maxBattery", "5000")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void filterByScreenSize_ShouldReturnFiltered() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.filterByScreenSizeWithLimit(any(), any(), eq(5))).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/filter/screen")
                .param("screenSizeOptions", "6.1")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void filterByOS_ShouldReturnFiltered() throws Exception {
        ProductCardResponse productCard = new ProductCardResponse();
        productCard.setId(1L);

        when(productViewService.filterByOSWithLimit(any(), any(), eq(5))).thenReturn(Collections.singletonList(productCard));

        mockMvc.perform(get("/api/v1/products/filter/os")
                .param("osOptions", "iOS")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }
}
