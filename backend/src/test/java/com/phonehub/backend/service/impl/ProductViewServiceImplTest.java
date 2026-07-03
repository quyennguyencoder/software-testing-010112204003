package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.productview.ProductFilterRequest;
import com.phonehub.backend.dto.response.productview.ProductCardResponse;
import com.phonehub.backend.dto.response.productview.ProductDetailViewResponse;
import com.phonehub.backend.entity.Category;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.CategoryRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.repository.ReviewRepository;
import com.phonehub.backend.service.intf.IPromotionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductViewServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private IPromotionService promotionService;

    @InjectMocks
    private ProductViewServiceImpl productViewService;

    @Test
    @DisplayName("Nên lấy danh sách sản phẩm nổi bật thành công")
    void getFeaturedProducts_Success() {
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("iPhone 15 Pro Max");
        mockProduct.setStatus(true);
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Smartphone");
        mockProduct.setCategory(cat);
        mockProduct.setTemplates(Collections.emptyList());
        mockProduct.setImages(Collections.emptyList());
        mockProduct.setMetadata(null);

        when(reviewRepository.findProductIdsWithHighRating(4.5)).thenReturn(Collections.singletonList(new Object[]{1L, 4.8}));
        when(productRepository.findByIdsWithDetails(anyList())).thenReturn(new java.util.ArrayList<>(List.of(mockProduct)));
        
        lenient().when(promotionService.getBestDiscountForProduct(any(), any(), any())).thenReturn(null);

        List<ProductCardResponse> response = productViewService.getFeaturedProducts(10);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("iPhone 15 Pro Max", response.get(0).getName());
    }

    @Test
    @DisplayName("Nên lấy chi tiết sản phẩm thành công")
    void getProductDetail_Success() {
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("iPhone 15 Pro Max");
        mockProduct.setStatus(true);
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Smartphone");
        mockProduct.setCategory(cat);
        mockProduct.setTemplates(Collections.emptyList());
        mockProduct.setImages(Collections.emptyList());
        mockProduct.setMetadata(null);

        when(productRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(mockProduct));
        when(reviewRepository.countReviewsByProductId(1L)).thenReturn(100L);
        when(reviewRepository.calculateAverageRatingByProductId(1L)).thenReturn(4.5);
        lenient().when(promotionService.getBestDiscountForProduct(any(), any(), any())).thenReturn(10.0);

        ProductDetailViewResponse response = productViewService.getProductDetailById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("iPhone 15 Pro Max", response.getName());
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi minPrice lớn hơn maxPrice")
    void filterProducts_ThrowsException_WhenMinPriceGreaterThanMaxPrice() {
        ProductFilterRequest request = ProductFilterRequest.builder()
                .minPrice(BigDecimal.valueOf(30000000))
                .maxPrice(BigDecimal.valueOf(10000000))
                .build();

        assertThrows(BadRequestException.class, () -> productViewService.filterProducts(request));
    }

    @Test
    @DisplayName("Nên ném ResourceNotFoundException khi không tìm thấy sản phẩm")
    void getProductDetail_ThrowsException_WhenNotFound() {
        when(productRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productViewService.getProductDetailById(1L));
    }
}
