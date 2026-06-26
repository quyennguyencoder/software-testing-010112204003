package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.brand.UpdateBrandRequest;
import com.phonehub.backend.dto.request.brand.CreateBrandRequest;
import com.phonehub.backend.dto.response.brand.BrandResponse;
import com.phonehub.backend.entity.Brand;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.BrandRepository;
import com.phonehub.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.phonehub.backend.exception.BadRequestException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand brand1;
    private Brand brand2;

    @BeforeEach
    void setUp() {
        // Chuẩn bị dữ liệu mẫu trước mỗi bài test
        brand1 = new Brand();
        brand1.setId(1L);
        brand1.setName("Apple");
        // Nếu entity Brand của sếp có thêm trường nào bắt buộc (như status, logo...), sếp set thêm ở đây nhé.

        brand2 = new Brand();
        brand2.setId(2L);
        brand2.setName("Samsung");
    }

    // ====================================================================================
    // TEST: getAllBrands()
    // ====================================================================================
    // Kiểm tra hàm getAllBrands() trả về danh sách brand với số lượng sản phẩm đúng
    @Test
    void getAllBrands_ShouldReturnListOfBrands_WithCorrectProductCount() {
        // database trả về 2 brand
        when(brandRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList(brand1, brand2));
        
        // số lượng sản phẩm cho từng brand
        when(productRepository.countByBrandIdAndIsDeletedFalse(1L)).thenReturn(15L);
        when(productRepository.countByBrandIdAndIsDeletedFalse(2L)).thenReturn(8L);
        // Gọi hàm cần test
        List<BrandResponse> result = brandService.getAllBrands();
        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.size());

        // Kiểm tra Brand 1 (Apple)
        assertEquals(1L, result.get(0).getId()); 
        assertEquals("Apple", result.get(0).getName());
        assertEquals(15L, result.get(0).getProductCount());

        // Kiểm tra Brand 2 (Samsung)
        assertEquals(2L, result.get(1).getId());
        assertEquals("Samsung", result.get(1).getName());
        assertEquals(8L, result.get(1).getProductCount());

        verify(brandRepository, times(1)).findAllByOrderByNameAsc();
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(1L);
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(2L);
    }
    // Kiểm tra hàm getAllBrands() khi database trống
    @Test
    void getAllBrands_WhenDatabaseIsEmpty_ShouldReturnEmptyList() {
        when(brandRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());
        List<BrandResponse> result = brandService.getAllBrands();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(brandRepository, times(1)).findAllByOrderByNameAsc();
        verify(productRepository, never()).countByBrandIdAndIsDeletedFalse(anyLong());
    }
    // ====================================================================================
    // TEST: getBrandById
    // ====================================================================================
    // Kiểm tra hàm getBrandById() khi brand tồn tại
    @Test
    void getBrandById_WhenIdExists_ShouldReturnBrandResponse() {
        // tìm thấy brand với ID = 1L
        when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(brand1));
        when(productRepository.countByBrandIdAndIsDeletedFalse(1L)).thenReturn(15L);
        // Gọi hàm cần test
        BrandResponse result = brandService.getBrandById(1L);

        // Kiểm tra xem dữ liệu trả về có đúng của Apple không
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Apple", result.getName());
        assertEquals(15L, result.getProductCount());

        verify(brandRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(1L);
    }
    // Kiểm tra hàm getBrandById() khi brand không tồn tại, hệ thống phải ném ra ResourceNotFoundException
    @Test
    void getBrandById_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // KHÔNG tìm thấy brand với ID = 99L 
        when(brandRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        // kiểm tra xem hệ thống có văng đúng lỗi ResourceNotFoundException không
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            brandService.getBrandById(99L);
        });
        assertEquals("Thương hiệu không tồn tại với ID: 99", exception.getMessage());
        verify(brandRepository, times(1)).findById(99L);
        verify(productRepository, never()).countByBrandIdAndIsDeletedFalse(anyLong());
    }
    // ====================================================================================
    // TEST: createBrand
    // ====================================================================================
    @Test
    void createBrand_WhenValidRequest_ShouldCreateAndReturnResponse() {
        CreateBrandRequest request = new CreateBrandRequest();
        request.setName("Nokia");
        request.setDescription("Connecting people");
        request.setLogoUrl("nokia.png");

        when(brandRepository.existsByName("Nokia")).thenReturn(false);

        Brand savedBrand = new Brand();
        savedBrand.setId(3L);
        savedBrand.setName("Nokia");
        savedBrand.setDescription("Connecting people");
        savedBrand.setLogoUrl("nokia.png");
        
        when(brandRepository.save(any(Brand.class))).thenReturn(savedBrand);

        BrandResponse result = brandService.createBrand(request);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Nokia", result.getName());
        assertEquals(0L, result.getProductCount()); 

        // Xác minh các bước đã chạy đủ
        verify(brandRepository, times(1)).existsByName("Nokia");
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    @Test
    void createBrand_WhenNameAlreadyExists_ShouldThrowBadRequestException() {
        CreateBrandRequest request = new CreateBrandRequest();
        request.setName("Apple");

        // Giả lập DB báo là tên này có rồi
        when(brandRepository.existsByName("Apple")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            brandService.createBrand(request);
        });

        // Kiểm tra đúng câu báo lỗi của sếp không
        assertEquals("Tên thương hiệu 'Apple' đã tồn tại", exception.getMessage());

        verify(brandRepository, times(1)).existsByName("Apple");
        verify(brandRepository, never()).save(any(Brand.class));
    }
    // ====================================================================================
    // TEST: updateBrand
    // ====================================================================================
    @Test
    void updateBrand_WhenValidRequest_ShouldUpdateAndReturnResponse() {
        UpdateBrandRequest request = new UpdateBrandRequest();
        request.setName("Apple V2");
        request.setDescription("Think Different Better");
        request.setLogoUrl("apple_v2.png");

        when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(brand1));
        when(brandRepository.existsByNameAndIdNot("Apple V2", 1L)).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand1);
        when(productRepository.countByBrandIdAndIsDeletedFalse(1L)).thenReturn(15L);
        BrandResponse result = brandService.updateBrand(1L, request);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Apple V2", result.getName());
        assertEquals("Think Different Better", result.getDescription());
        assertEquals(15L, result.getProductCount());

        verify(brandRepository, times(1)).findById(1L);
        verify(brandRepository, times(1)).existsByNameAndIdNot("Apple V2", 1L);
        verify(brandRepository, times(1)).save(any(Brand.class));
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(1L);
    }
    // Kiểm tra hàm updateBrand() khi brand không tồn tại, hệ thống phải ném ra ResourceNotFoundException
    @Test
    void updateBrand_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        UpdateBrandRequest request = new UpdateBrandRequest();
        request.setName("Apple V2");

        when(brandRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            brandService.updateBrand(99L, request);
        });

        assertEquals("Thương hiệu không tồn tại với ID: 99", exception.getMessage());
        
        // Chặn lại luôn, không chạy xuống các hàm dưới
        verify(brandRepository, times(1)).findById(99L);
        verify(brandRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(brandRepository, never()).save(any(Brand.class));
    }
    // Kiểm tra hàm updateBrand() khi brand tồn tại, hệ thống phải ném ra BadRequestException
    @Test
    void updateBrand_WhenNewNameAlreadyExists_ShouldThrowBadRequestException() {
        // 1. Arrange
        UpdateBrandRequest request = new UpdateBrandRequest();
        request.setName("Samsung"); 

        when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(brand1));
        when(brandRepository.existsByNameAndIdNot("Samsung", 1L)).thenReturn(true);

        // 2. Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            brandService.updateBrand(1L, request);
        });

        assertEquals("Tên thương hiệu 'Samsung' đã tồn tại", exception.getMessage());

        verify(brandRepository, times(1)).findById(1L);
        verify(brandRepository, times(1)).existsByNameAndIdNot("Samsung", 1L);
        verify(brandRepository, never()).save(any(Brand.class)); 
    }
    // ====================================================================================
    // TEST: deleteBrand
    // ====================================================================================
    @Test
    void deleteBrand_WhenValidAndNoProducts_ShouldDeleteSuccessfully() {
        when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(brand1));
        
        when(productRepository.countByBrandIdAndIsDeletedFalse(1L)).thenReturn(0L);

        brandService.deleteBrand(1L);

        verify(brandRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(1L);
        verify(brandRepository, times(1)).delete(brand1);
    }
    // Kiểm tra hàm deleteBrand() khi brand không tồn tại, hệ thống phải ném ra ResourceNotFoundException
    @Test
    void deleteBrand_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(brandRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            brandService.deleteBrand(99L);
        });

        assertEquals("Thương hiệu không tồn tại với ID: 99", exception.getMessage());
        verify(brandRepository, times(1)).findById(99L);
        verify(productRepository, never()).countByBrandIdAndIsDeletedFalse(anyLong());
        verify(brandRepository, never()).delete(any(Brand.class));
    }

    @Test
    void deleteBrand_WhenBrandHasProducts_ShouldThrowBadRequestException() {
        when(brandRepository.findById(1L)).thenReturn(java.util.Optional.of(brand1));
        
        when(productRepository.countByBrandIdAndIsDeletedFalse(1L)).thenReturn(5L);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            brandService.deleteBrand(1L);
        });

        assertEquals("Không thể xóa thương hiệu. Thương hiệu đang có 5 sản phẩm liên kết", exception.getMessage());
        verify(brandRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).countByBrandIdAndIsDeletedFalse(1L);
        verify(brandRepository, never()).delete(any(Brand.class));
    }
}   