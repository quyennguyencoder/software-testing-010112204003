package com.phonehub.backend;

import com.phonehub.backend.dto.request.product.CreateProductRequest;
import com.phonehub.backend.dto.request.product.ProductMetadataRequest;
import com.phonehub.backend.dto.request.product.ProductTemplateRequest;
import com.phonehub.backend.dto.request.product.UpdateProductRequest;
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
import java.util.Arrays;
import java.math.BigDecimal;
import java.util.ArrayList;
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
        verify(productMapper, never()).toDetailResponse(any());
    }

    // ==============================================================================
    // TEST: updateProduct
    // ==============================================================================
    @Test
    void updateProduct_Success_FullUpdate() {
        // Cập nhật đầy đủ thông tin (Name, Category, Brand, Templates, Metadata)
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Name");
        existingProduct.setMetadata(new ProductMetadata()); 

        UpdateProductRequest updateReq = new UpdateProductRequest();
        updateReq.setName("New Name");
        updateReq.setCategoryId(2L);
        updateReq.setBrandId(2L);
        
        ProductTemplateRequest templateReq = new ProductTemplateRequest();
        templateReq.setSku("NEW-SKU-1");
        updateReq.setTemplates(Collections.singletonList(templateReq));
        
        ProductMetadataRequest metadataReq = new ProductMetadataRequest();
        updateReq.setMetadata(metadataReq);

        // Dàn xếp DB
        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByNameAndNotDeleted("New Name", productId)).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(new Category()));
        when(brandRepository.findById(2L)).thenReturn(Optional.of(new Brand()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        
        // Dàn xếp Template & EntityManager
        when(productTemplateRepository.existsBySku("NEW-SKU-1")).thenReturn(false);
        doNothing().when(entityManager).flush(); 
        when(productTemplateMapper.toEntity(any())).thenReturn(new ProductTemplate());
        
        // Dàn xếp Save & Return
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
        when(productMapper.toDetailResponse(existingProduct)).thenReturn(new ProductDetailResponse());

        // Gọi hàm
        ProductDetailResponse result = productService.updateProduct(productId, updateReq, 1L);

        assertNotNull(result);
        verify(productRepository, times(1)).save(existingProduct);
        // Kiểm tra xem có đi vào nhánh update existing metadata không
        verify(productMetadataMapper, times(1)).updateEntityFromRequest(metadataReq, existingProduct.getMetadata());
    }
    // Cập nhật một phần (Chỉ đổi tên, không đổi Brand/Category/Templates, tạo Metadata MỚI)
    @Test
    void updateProduct_Success_PartialUpdate_NewMetadata() {
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Name");
        existingProduct.setMetadata(null); 

        UpdateProductRequest updateReq = new UpdateProductRequest();
        updateReq.setName("Old Name"); 
        updateReq.setMetadata(new ProductMetadataRequest()); // Update metadata

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        
        // Nhánh tạo mới metadata
        when(productMetadataMapper.toEntity(any())).thenReturn(new ProductMetadata());
        
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
        when(productMapper.toDetailResponse(existingProduct)).thenReturn(new ProductDetailResponse());

        ProductDetailResponse result = productService.updateProduct(productId, updateReq, 1L);

        assertNotNull(result);
        verify(productRepository, times(1)).save(existingProduct);
        verify(productRepository, never()).existsByNameAndNotDeleted(any(), any()); // Không check tên
        verify(entityManager, never()).flush(); // Không đụng tới templates
    }
    // Kiểm tra luồng chạy khi truyền vào một ID sản phẩm không tồn tại.
    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Sản phẩm không tồn tại
        Long invalidProductId = 99L;
        UpdateProductRequest updateReq = new UpdateProductRequest();

        when(productRepository.findByIdAndIsDeletedFalse(invalidProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(invalidProductId, updateReq, 1L);
        });

        verify(productRepository, never()).save(any());
    }
    // Lỗi trùng SKU của sản phẩm khác
    @Test
    void updateProduct_DuplicateSkuFromAnotherProduct_ThrowsException() {
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);

        UpdateProductRequest updateReq = new UpdateProductRequest();
        ProductTemplateRequest templateReq = new ProductTemplateRequest();
        templateReq.setSku("DUPLICATE-SKU");
        updateReq.setTemplates(Collections.singletonList(templateReq));

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        
        // SKU đã tồn tại và thuộc về Product mang ID số 2 
        when(productTemplateRepository.existsBySku("DUPLICATE-SKU")).thenReturn(true);
        Product anotherProduct = new Product();
        anotherProduct.setId(2L); 
        ProductTemplate existingTemplate = new ProductTemplate();
        existingTemplate.setProduct(anotherProduct);
        
        when(productTemplateRepository.findBySku("DUPLICATE-SKU")).thenReturn(Optional.of(existingTemplate));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.updateProduct(productId, updateReq, 1L);
        });

        assertTrue(exception.getMessage().contains("SKU đã tồn tại"));
        verify(entityManager, never()).flush(); 
    }
    // ==============================================================================
    // TEST: deleteProduct
    // ============================================================================== 
    // Test case Thành công: Bật cờ xóa mềm thành công
    @Test
    void deleteProduct_Success() {
        // Chuẩn bị dữ liệu 
        Long productId = 1L;
        Long userId = 2L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setIsDeleted(false); // Chưa bị xóa

        User mockUser = new User();
        mockUser.setId(userId);

        // Giả lập DB trả về đúng sản phẩm và user
        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Thực thi (When)
        productService.deleteProduct(productId, userId);

        // Bắt buộc phải xác nhận cờ isDeleted đã được chuyển thành true
        assertTrue(existingProduct.getIsDeleted());
        assertNotNull(existingProduct.getDeletedAt()); // Đã ghi nhận thời gian xóa
        assertEquals(userId, existingProduct.getDeletedBy().getId()); 

        // Xác nhận hàm save được gọi đúng 1 lần để lưu trạng thái
        verify(productRepository, times(1)).save(existingProduct);
    }

    // Sản phẩm không tồn tại
    @Test
    void deleteProduct_ProductNotFound_ThrowsException() {
        Long invalidProductId = 99L;
        Long userId = 2L;

        // Giả lập không tìm thấy sản phẩm
        when(productRepository.findByIdAndIsDeletedFalse(invalidProductId)).thenReturn(Optional.empty());

        // Thực thi và bẫy lỗi
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(invalidProductId, userId);
        });

        // Xác nhận tuyệt đối không được gọi hàm save
        verify(productRepository, never()).save(any());
    }

    // Người dùng (Admin/Staff) không tồn tại
    @Test
    void deleteProduct_UserNotFound_ThrowsException() {
        Long productId = 1L;
        Long invalidUserId = 99L;

        Product existingProduct = new Product();
        existingProduct.setId(productId);

        // Giả lập tìm thấy sản phẩm, nhưng user thì rỗng
        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // Thực thi và bẫy lỗi
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(productId, invalidUserId);
        });

        // Trạng thái xóa chưa được kích hoạt, cấm gọi DB lưu
        verify(productRepository, never()).save(any());
    }
    // ==============================================================================
    // TEST: increaseStock
    // ============================================================================== 
    // Kịch bản Thành công
    @Test
    void increaseStock_Success() {
        Long productId = 1L;
        Integer amountToAdd = 5; 

        Product existingProduct = new Product();
        existingProduct.setId(productId);

        // Tạo 2 cái phiên bản để test xem vòng lặp for có chạy đúng không
        ProductTemplate template1 = new ProductTemplate();
        template1.setStockQuantity(10); // Đang có 10

        ProductTemplate template2 = new ProductTemplate();
        template2.setStockQuantity(20); // Đang có 20

        existingProduct.setTemplates(Arrays.asList(template1, template2));
        

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Thực thi
        productService.increaseStock(productId, amountToAdd);

        // Kiểm chứng: 10 + 5 = 15; 20 + 5 = 25
        assertEquals(15, template1.getStockQuantity());
        assertEquals(25, template2.getStockQuantity());
        
        verify(productRepository, times(1)).save(existingProduct);
    }

    // Truyền số lượng âm hoặc bằng 0
    @Test
    void increaseStock_InvalidAmount_ThrowsException() {
        Long productId = 1L;
        Integer invalidAmount = -5; // Cố tình truyền số âm

        // Thực thi và bẫy lỗi 
        assertThrows(BadRequestException.class, () -> {
            productService.increaseStock(productId, invalidAmount);
        });

        // Bị chặn ở Trạm 1 nên tuyệt đối không được gọi xuống DB
        verify(productRepository, never()).findByIdAndIsDeletedFalse(any());
        verify(productRepository, never()).save(any());
    }

    // Sản phẩm không tồn tại
    @Test
    void increaseStock_ProductNotFound_ThrowsException() {
        Long invalidProductId = 99L;
        Integer amountToAdd = 5;

        when(productRepository.findByIdAndIsDeletedFalse(invalidProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.increaseStock(invalidProductId, amountToAdd);
        });

        verify(productRepository, never()).save(any());
    }

    // Sản phẩm không có biến thể (Templates rỗng)
    @Test
    void increaseStock_NoTemplates_ThrowsException() {
        Long productId = 1L;
        Integer amountToAdd = 5;

        Product productWithoutTemplates = new Product();
        productWithoutTemplates.setId(productId);
        productWithoutTemplates.setTemplates(new ArrayList<>()); 

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(productWithoutTemplates));

        // validateProductHasTemplates ném ra
        assertThrows(BadRequestException.class, () -> {
            productService.increaseStock(productId, amountToAdd);
        });

        verify(productRepository, never()).save(any());
    }
    // ==============================================================================
    // TEST: decreaseStock
    // ==============================================================================
    // Kịch bản Thành công: Thuật toán trừ lùi và break vòng lặp hoạt động chuẩn
    @Test
    void decreaseStock_Success_DeductSequentially() {
        Long productId = 1L;
        Integer amountToDecrease = 12; // mua 12 cái

        Product existingProduct = new Product();
        existingProduct.setId(productId);

        //  tạo 3 biến thể 
        ProductTemplate template1 = new ProductTemplate();
        template1.setStockQuantity(2);  // Ít nhất

        ProductTemplate template2 = new ProductTemplate();
        template2.setStockQuantity(10); // Nhiều nhất 

        ProductTemplate template3 = new ProductTemplate();
        template3.setStockQuantity(5);  // Trung bình 

        // Tổng kho = 17 (Đủ để trừ 12)
        existingProduct.setTemplates(Arrays.asList(template1, template2, template3));

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Thực thi
        productService.decreaseStock(productId, amountToDecrease);

        assertEquals(0, template2.getStockQuantity()); 
        assertEquals(3, template3.getStockQuantity()); 
        assertEquals(2, template1.getStockQuantity()); 
        
        verify(productRepository, times(1)).save(existingProduct);
    }

    // Lỗi: Truyền số lượng âm hoặc bằng 0
    @Test
    void decreaseStock_InvalidAmount_ThrowsException() {
        Long productId = 1L;
        Integer invalidAmount = -5; 

        assertThrows(BadRequestException.class, () -> {
            productService.decreaseStock(productId, invalidAmount);
        });
        verify(productRepository, never()).findByIdAndIsDeletedFalse(any());
    }
    // Lỗi: Sản phẩm không tồn tại 
    @Test
    void decreaseStock_ProductNotFound_ThrowsException() {
        Long invalidProductId = 99L;
        Integer amountToDecrease = 5;

        when(productRepository.findByIdAndIsDeletedFalse(invalidProductId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.decreaseStock(invalidProductId, amountToDecrease);
        });
    }
    // Lỗi: Sản phẩm không có biến thể nào 
    @Test
    void decreaseStock_NoTemplates_ThrowsException() {
        Long productId = 1L;
        Integer amountToDecrease = 5;

        Product productWithoutTemplates = new Product();
        productWithoutTemplates.setId(productId);
        productWithoutTemplates.setTemplates(new ArrayList<>()); 

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(productWithoutTemplates));

        assertThrows(BadRequestException.class, () -> {
            productService.decreaseStock(productId, amountToDecrease);
        });
    } 
    // Lỗi: Không đủ hàng trong kho 
    @Test
    void decreaseStock_InsufficientTotalStock_ThrowsException() {
        Long productId = 1L;
        Integer amountToDecrease = 50; // mua 50 cái

        Product existingProduct = new Product();
        existingProduct.setId(productId);

        ProductTemplate template1 = new ProductTemplate();
        template1.setStockQuantity(10); // Kho chỉ có 10

        existingProduct.setTemplates(Collections.singletonList(template1));

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(existingProduct));
        assertThrows(BadRequestException.class, () -> {
            productService.decreaseStock(productId, amountToDecrease);
        });

        // Xác nhận không lưu bậy bạ xuống DB
        verify(productRepository, never()).save(any());
    } 
}
