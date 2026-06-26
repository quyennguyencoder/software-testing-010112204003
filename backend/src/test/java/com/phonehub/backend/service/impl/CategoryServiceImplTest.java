package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.category.CreateCategoryRequest;
import com.phonehub.backend.dto.request.category.UpdateCategoryRequest;
import com.phonehub.backend.dto.response.category.CategoryResponse;
import com.phonehub.backend.entity.Category;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.CategoryRepository;
import com.phonehub.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private Category testParentCategory;
    private CreateCategoryRequest createRequest;
    private UpdateCategoryRequest updateRequest;

    @BeforeEach
    void setUp() {
        testParentCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();

        testCategory = Category.builder()
                .id(2L)
                .name("Smartphones")
                .description("Mobile phones")
                .parent(testParentCategory)
                .build();

        createRequest = CreateCategoryRequest.builder()
                .name("Laptops")
                .description("Portable computers")
                .parentId(1L)
                .build();

        updateRequest = UpdateCategoryRequest.builder()
                .name("Smartphones Updated")
                .description("Updated mobile phones")
                .parentId(1L)
                .build();
    }

    @Test
    void getCategoriesByParentId_RootCategories_Success() {
        when(categoryRepository.findByParentIdIsNull()).thenReturn(Collections.singletonList(testParentCategory));
        when(categoryRepository.countByParentId(1L)).thenReturn(5L);
        when(productRepository.countByCategoryIdAndIsDeletedFalse(1L)).thenReturn(10L);

        List<CategoryResponse> responses = categoryService.getCategoriesByParentId(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testParentCategory.getName(), responses.get(0).getName());
        assertEquals(5, responses.get(0).getChildrenCount());
        assertEquals(10, responses.get(0).getProductCount());
        assertTrue(responses.get(0).getHasChildren());

        verify(categoryRepository, times(1)).findByParentIdIsNull();
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void getCategoriesByParentId_ChildCategories_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findByParentId(1L)).thenReturn(Collections.singletonList(testCategory));
        when(categoryRepository.countByParentId(2L)).thenReturn(0L);
        when(productRepository.countByCategoryIdAndIsDeletedFalse(2L)).thenReturn(20L);

        List<CategoryResponse> responses = categoryService.getCategoriesByParentId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testCategory.getName(), responses.get(0).getName());
        assertEquals(0, responses.get(0).getChildrenCount());
        assertEquals(20, responses.get(0).getProductCount());
        assertFalse(responses.get(0).getHasChildren());

        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).findByParentId(1L);
    }

    @Test
    void getCategoriesByParentId_ParentNotFound_ThrowsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> categoryService.getCategoriesByParentId(99L));

        assertEquals("Danh mục cha không tồn tại với ID: 99", exception.getMessage());
        verify(categoryRepository, times(1)).existsById(99L);
        verify(categoryRepository, never()).findByParentId(anyLong());
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByNameAndParentId(createRequest.getName(), createRequest.getParentId())).thenReturn(false);
        when(categoryRepository.findById(createRequest.getParentId())).thenReturn(Optional.of(testParentCategory));
        
        Category savedCategory = Category.builder()
                .id(3L)
                .name(createRequest.getName())
                .description(createRequest.getDescription())
                .parent(testParentCategory)
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepository.countByParentId(3L)).thenReturn(0L);
        when(productRepository.countByCategoryIdAndIsDeletedFalse(3L)).thenReturn(0L);

        CategoryResponse response = categoryService.createCategory(createRequest);

        assertNotNull(response);
        assertEquals(3L, response.getId());
        assertEquals(createRequest.getName(), response.getName());
        assertEquals(testParentCategory.getId(), response.getParentId());

        verify(categoryRepository, times(1)).existsByNameAndParentId(createRequest.getName(), createRequest.getParentId());
        verify(categoryRepository, times(1)).findById(createRequest.getParentId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_NameExists_ThrowsException() {
        when(categoryRepository.existsByNameAndParentId(createRequest.getName(), createRequest.getParentId())).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> categoryService.createCategory(createRequest));

        assertEquals("Tên danh mục 'Laptops' đã tồn tại trong danh mục cha ID 1", exception.getMessage());
        verify(categoryRepository, times(1)).existsByNameAndParentId(createRequest.getName(), createRequest.getParentId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_Success() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndParentIdAndIdNot(updateRequest.getName(), updateRequest.getParentId(), 2L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testParentCategory));
        
        Category savedCategory = Category.builder()
                .id(2L)
                .name(updateRequest.getName())
                .description(updateRequest.getDescription())
                .parent(testParentCategory)
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepository.countByParentId(2L)).thenReturn(2L);
        when(productRepository.countByCategoryIdAndIsDeletedFalse(2L)).thenReturn(15L);

        CategoryResponse response = categoryService.updateCategory(2L, updateRequest);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(updateRequest.getName(), response.getName());
        assertEquals(testParentCategory.getId(), response.getParentId());
        assertEquals(2, response.getChildrenCount());
        assertEquals(15, response.getProductCount());

        verify(categoryRepository, times(2)).findById(anyLong());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void updateCategory_CircularReference_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testParentCategory));
        when(categoryRepository.existsByNameAndParentIdAndIdNot(updateRequest.getName(), 1L, 1L)).thenReturn(false);

        UpdateCategoryRequest circularRequest = UpdateCategoryRequest.builder()
                .name(updateRequest.getName())
                .parentId(1L) // Setting parent to itself
                .build();

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> categoryService.updateCategory(1L, circularRequest));

        assertEquals("Danh mục không thể là cha của chính nó", exception.getMessage());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.countByParentId(2L)).thenReturn(0L);
        when(productRepository.existsByCategoryId(2L)).thenReturn(false);
        doNothing().when(categoryRepository).delete(testCategory);

        categoryService.deleteCategory(2L);

        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, times(1)).countByParentId(2L);
        verify(productRepository, times(1)).existsByCategoryId(2L);
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void deleteCategory_HasChildren_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testParentCategory));
        when(categoryRepository.countByParentId(1L)).thenReturn(5L);

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> categoryService.deleteCategory(1L));

        assertEquals("Không thể xóa danh mục. Danh mục này có 5 danh mục con", exception.getMessage());
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).countByParentId(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_HasProducts_ThrowsException() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.countByParentId(2L)).thenReturn(0L);
        when(productRepository.existsByCategoryId(2L)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, 
                () -> categoryService.deleteCategory(2L));

        assertEquals("Không thể xóa danh mục. Danh mục đang chứa sản phẩm", exception.getMessage());
        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, times(1)).countByParentId(2L);
        verify(productRepository, times(1)).existsByCategoryId(2L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
