package com.phonehub.backend;

import com.phonehub.backend.dto.request.product.CreateProductRequest;
import com.phonehub.backend.dto.request.product.ProductMetadataRequest;
import com.phonehub.backend.dto.request.product.ProductTemplateRequest;
import com.phonehub.backend.dto.response.product.ProductDetailResponse;
import com.phonehub.backend.dto.response.product.ProductTemplateResponse;
import com.phonehub.backend.entity.Brand;
import com.phonehub.backend.entity.Category;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.entity.ProductMetadata;
import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.mapper.ProductMapper;
import com.phonehub.backend.mapper.ProductMetadataMapper;
import com.phonehub.backend.mapper.ProductTemplateMapper;
import com.phonehub.backend.repository.BrandRepository;
import com.phonehub.backend.repository.CategoryRepository;
import com.phonehub.backend.repository.ProductImageRepository;
import com.phonehub.backend.repository.ProductMetadataRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.repository.ProductTemplateRepository;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.service.impl.ProductServiceImpl;
import com.phonehub.backend.service.intf.IPromotionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductMetadataRepository productMetadataRepository;

    @Mock
    private ProductTemplateMapper productTemplateMapper;

    @Mock
    private ProductMetadataMapper productMetadataMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private IPromotionService promotionService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private Brand testBrand;
    private User testUser;
    private CreateProductRequest createProductRequest;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Category");

        testBrand = new Brand();
        testBrand.setId(1L);
        testBrand.setName("Brand");

        testUser = new User();
        testUser.setId(1L);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setCategory(testCategory);
        testProduct.setBrand(testBrand);

        ProductTemplateRequest templateReq = new ProductTemplateRequest();
        templateReq.setSku("SKU-1");
        
        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("New Product");
        createProductRequest.setCategoryId(1L);
        createProductRequest.setBrandId(1L);
        createProductRequest.setTemplates(Collections.singletonList(templateReq));
    } 
    
    // --- Test for getProductMetadataGreaterThanPrice method --- //
    // Kiểm tra luồng chạy chính của hàm khi truyền vào một mức giá hợp lệ (100).
    @Test
    void getProductMetadataGreaterThanPrice_Success() {
        BigDecimal price = BigDecimal.valueOf(100);
        ProductTemplate template = new ProductTemplate();
        when(productTemplateRepository.findByPriceGreaterThan(price)).thenReturn(Collections.singletonList(template));
        
        ProductTemplateResponse response = new ProductTemplateResponse();
        when(productTemplateMapper.toResponseList(anyList())).thenReturn(Collections.singletonList(response));

        List<ProductTemplateResponse> result = productService.getProductMetadataGreaterThanPrice(price);

        assertNotNull(result); // Đảm bảo kết quả không bị null
        assertEquals(1, result.size()); // Đảm bảo số lượng phần tử trả về khớp với Mock data
    }
    // Kiểm tra luồng chạy khi truyền vào một mức giá âm (-50), mong đợi trả về danh sách rỗng.
    @Test
    void getProductMetadataGreaterThanPrice_NegativePrice() {
        BigDecimal price = BigDecimal.valueOf(-50);

        when(productTemplateRepository.findByPriceGreaterThan(price)).thenReturn(Collections.emptyList());
        when(productTemplateMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ProductTemplateResponse> result = productService.getProductMetadataGreaterThanPrice(price);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    // Kiểm tra luồng chạy khi truyền vào mức giá bằng 0, mong đợi trả về danh sách chứa các sản phẩm có giá lớn hơn 0.
    @Test
    void getProductMetadataGreaterThanPrice_ZeroPrice() {
        BigDecimal price = BigDecimal.ZERO;

        ProductTemplate template = new ProductTemplate();
        when(productTemplateRepository.findByPriceGreaterThan(price)).thenReturn(Collections.singletonList(template));
        
        ProductTemplateResponse response = new ProductTemplateResponse();
        when(productTemplateMapper.toResponseList(anyList())).thenReturn(Collections.singletonList(response));

        List<ProductTemplateResponse> result = productService.getProductMetadataGreaterThanPrice(price);

        assertNotNull(result);
        assertEquals(1, result.size()); 
    }
    // Kiểm tra luồng chạy khi truyền vào mức giá rất lớn (999999999), mong đợi trả về danh sách rỗng.
    @Test
    void getProductMetadataGreaterThanPrice_MaxPrice() {
        BigDecimal price = BigDecimal.valueOf(999999999);

        when(productTemplateRepository.findByPriceGreaterThan(price)).thenReturn(Collections.emptyList());
        when(productTemplateMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ProductTemplateResponse> result = productService.getProductMetadataGreaterThanPrice(price);

        assertNotNull(result);
        assertTrue(result.isEmpty()); 
    }
    // Kiểm tra luồng chạy khi truyền vào mức giá null, mong đợi ném ra BadRequestException.
    @Test
    void getProductMetadataGreaterThanPrice_NullPrice_ThrowsException() {
        BigDecimal invalidPrice = null;

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.getProductMetadataGreaterThanPrice(invalidPrice);
        });

        assertTrue(exception.getMessage().contains("Giá tiền không hợp lệ"));


        verify(productTemplateRepository, never()).findByPriceGreaterThan(any());
    }
}
