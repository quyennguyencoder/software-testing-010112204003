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
    
    // ==============================================================================
    // TEST: createProduct
    // ==============================================================================
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
    void getProductMetadataGreaterThanPrice_NegativePrice_ThrowsException() {
        BigDecimal price = BigDecimal.valueOf(-50);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.getProductMetadataGreaterThanPrice(price);
        });

        assertTrue(exception.getMessage().contains("Giá tiền không hợp lệ"));

        verify(productTemplateRepository, never()).findByPriceGreaterThan(any());
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

    // ==============================================================================
    // TEST: createProduct
    // ==============================================================================
    @Test
    void createProduct_Success_NoMetadata() {
        // Khởi tạo sản phẩm thành công (Không kèm Metadata)
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Vượt qua vòng check trùng tên và trùng SKU
        when(productRepository.existsByNameAndNotDeleted(createProductRequest.getName(), null)).thenReturn(false);
        when(productTemplateRepository.existsBySku("SKU-1")).thenReturn(false);
        
        // Dàn xếp Mapper và Save
        when(productMapper.toEntity(createProductRequest)).thenReturn(new Product());
        when(productTemplateMapper.toEntity(any())).thenReturn(new ProductTemplate());
        
        Product savedProduct = new Product();
        savedProduct.setId(1L);
        // Cần khởi tạo list rỗng để code gốc gọi .size() không bị lỗi NullPointer
        savedProduct.setTemplates(Collections.singletonList(new ProductTemplate())); 
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(savedProduct)).thenReturn(new ProductDetailResponse());

        // Chạy hàm thực tế
        ProductDetailResponse result = productService.createProduct(createProductRequest, 1L);

        // Nghiệm thu
        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
        // Đảm bảo mapper metadata 
        verify(productMetadataMapper, never()).toEntity(any()); 
    }
    // Kiểm tra luồng chạy chính của hàm khi truyền vào một CreateProductRequest hợp lệ, nhưng có kèm theo Metadata.
    @Test
    void createProduct_Success_WithMetadata() {
        // (request.getMetadata() != null)
        
        // Gắn thêm Metadata vào Request
        ProductMetadataRequest metadataRequest = new ProductMetadataRequest();
        createProductRequest.setMetadata(metadataRequest); 

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.existsByNameAndNotDeleted(createProductRequest.getName(), null)).thenReturn(false);
        when(productTemplateRepository.existsBySku("SKU-1")).thenReturn(false);
        
        when(productMapper.toEntity(createProductRequest)).thenReturn(new Product());
        when(productTemplateMapper.toEntity(any())).thenReturn(new ProductTemplate());
        
        when(productMetadataMapper.toEntity(metadataRequest)).thenReturn(new ProductMetadata());

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setTemplates(Collections.singletonList(new ProductTemplate()));
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(savedProduct)).thenReturn(new ProductDetailResponse());

        ProductDetailResponse result = productService.createProduct(createProductRequest, 1L);

        assertNotNull(result);
        verify(productMetadataMapper, times(1)).toEntity(metadataRequest);
    }
    // Kiểm tra luồng chạy khi truyền vào một CreateProductRequest có ID Danh mục (Category) không tồn tại, mong đợi ném ra ResourceNotFoundException.
    @Test
    void createProduct_CategoryNotFound_ThrowsException() {        
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Kỳ vọng hệ thống ném ra ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(createProductRequest, 1L);
        });
        verify(brandRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }
    // Kiểm tra luồng chạy khi truyền vào một CreateProductRequest có name trùng
    @Test
    void createProduct_DuplicateName_ThrowsException() {
        
        // Cho qua vòng check ID
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Cố tình báo là tên sản phẩm đã tồn tại
        when(productRepository.existsByNameAndNotDeleted(createProductRequest.getName(), null)).thenReturn(true);

        // Kỳ vọng văng BadRequestException do trùng tên
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.createProduct(createProductRequest, 1L);
        });

        verify(productTemplateRepository, never()).existsBySku(any());
        verify(productRepository, never()).save(any());
    }
    // ==============================================================================
    // TEST: getProductById
    // ==============================================================================
    @Test
    void getProductById_Success() {
        // Lấy thông tin chi tiết sản phẩm thành công
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        
        ProductDetailResponse mockResponse = new ProductDetailResponse();
        mockResponse.setId(productId);

        // Database tìm thấy sản phẩm
        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(mockProduct));
        
        // Mapper chuyển đổi thành công
        when(productMapper.toDetailResponse(mockProduct)).thenReturn(mockResponse);

        // Gọi hàm thực tế
        ProductDetailResponse result = productService.getProductById(productId);

        // Nghiệm thu kết quả
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository, times(1)).findByIdAndIsDeletedFalse(productId);
        verify(productMapper, times(1)).toDetailResponse(mockProduct);
    }
    // Kiểm tra luồng chạy khi truyền vào một ID sản phẩm không tồn tại, mong đợi ném ra ResourceNotFoundException.
    @Test
    void getProductById_NotFound_ThrowsException() {
        // Sản phẩm không tồn tại hoặc đã bị xóa mềm (IsDeleted = true)
        Long invalidProductId = 99L;

        // Database trả về rỗng
        when(productRepository.findByIdAndIsDeletedFalse(invalidProductId)).thenReturn(Optional.empty());

        // Kỳ vọng hệ thống sẽ ném ra ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(invalidProductId);
        });

        // Kiểm tra đúng câu lệnh của hệ thống chưa 
        assertTrue(exception.getMessage().contains("Không tìm thấy sản phẩm"));
        
        // bị văng lỗi giữa chừng nên Mapper không được gọi
        verify(productMapper, never()).toDetailResponse(any());
    }
}
