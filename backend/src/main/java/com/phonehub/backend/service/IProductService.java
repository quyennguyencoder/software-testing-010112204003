package com.phonehub.backend.service;

import com.phonehub.backend.dto.request.product.ManageImagesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

import com.phonehub.backend.dto.request.product.CreateProductRequest;
import com.phonehub.backend.dto.request.product.UpdateProductRequest;
import com.phonehub.backend.dto.response.product.ProductDetailResponse;
import com.phonehub.backend.dto.response.product.ProductListResponse;
import com.phonehub.backend.dto.response.product.ProductTemplateResponse;

/**
 * Service interface for Product operations
 */
public interface IProductService {
    // Get product metadata greater than price
    List<ProductTemplateResponse> getProductMetadataGreaterThanPrice(BigDecimal price);

    /**
     * Create a new product
     * 
     * @param request Product creation request
     * @param userId  ID of user creating the product (for audit)
     * @return Created product detail response
     */
    ProductDetailResponse createProduct(CreateProductRequest request, Long userId);

    /**
     * Get product by ID (Admin)
     * 
     * @param id Product ID
     * @return Product detail response
     */
    ProductDetailResponse getProductById(Long id);

    /**
     * Update an existing product
     * 
     * @param id      Product ID
     * @param request Product update request
     * @param userId  ID of user updating the product (for audit)
     * @return Updated product detail response
     */
    ProductDetailResponse updateProduct(Long id, UpdateProductRequest request, Long userId);

    /**
     * Soft delete a product
     * 
     * @param id     Product ID
     * @param userId ID of user deleting the product (for audit)
     */
    void deleteProduct(Long id, Long userId);

    /**
     * Increase product stock (used by Order service when order is cancelled)
     * 
     * @param id     Product ID
     * @param amount Amount to increase
     */
    void increaseStock(Long id, Integer amount);

    /**
     * Decrease product stock (used by Order service when order is created)
     * 
     * @param id     Product ID
     * @param amount Amount to decrease
     */
    void decreaseStock(Long id, Integer amount);

    /**
     * Get products with optional filtering, searching, and sorting
     * All parameters are optional. If no parameters provided, returns all active
     * products.
     * 
     * @param keyword        Search keyword (searches in name, SKU, description)
     * @param categoryId     Filter by category ID
     * @param brandId        Filter by brand ID
     * @param minPrice       Minimum price filter
     * @param maxPrice       Maximum price filter
     * @param status         Filter by status (true=active, false=inactive)
     * @param includeDeleted Include soft-deleted products (Admin only)
     * @param sortBy         Sort field (name, price, stock, createdAt)
     * @param sortDirection  Sort direction (asc, desc)
     * @param pageable       Pagination parameters
     * @return Page of product list responses
     */
    Page<ProductListResponse> getProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            Double minPrice,
            Double maxPrice,
            Boolean status,
            Boolean includeDeleted,
            String sortBy,
            String sortDirection,
            Pageable pageable);

    /**
     * Get deleted products (soft-deleted products with isDeleted=true)
     * 
     * @param keyword    Search keyword
     * @param categoryId Filter by category ID
     * @param brandId    Filter by brand ID
     * @param pageable   Pagination parameters
     * @return Page of deleted product list responses
     */
    Page<ProductListResponse> getDeletedProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            Pageable pageable);

    /**
     * Restore a soft-deleted product
     * 
     * @param id     Product ID
     * @param userId ID of user restoring the product
     */
    void restoreProduct(Long id, Long userId);

    /**
     * Manage product images (add/update/reorder)
     * 
     * @param productId Product ID
     * @param request   Image management request
     */
    void manageProductImages(Long productId, ManageImagesRequest request);

    /**
     * Delete a specific product image
     * 
     * @param productId Product ID
     * @param imageId   Image ID
     */
    void deleteProductImage(Long productId, Long imageId);
}
