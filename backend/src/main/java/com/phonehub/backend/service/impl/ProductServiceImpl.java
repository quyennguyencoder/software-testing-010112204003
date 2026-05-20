package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.product.CreateProductRequest;
import com.phonehub.backend.dto.request.product.ManageImagesRequest;
import com.phonehub.backend.dto.request.product.ProductImageRequest;
import com.phonehub.backend.dto.request.product.ProductTemplateRequest;
import com.phonehub.backend.dto.request.product.UpdateProductRequest;
import com.phonehub.backend.dto.response.product.ProductDetailResponse;
import com.phonehub.backend.dto.response.product.ProductImageResponse;
import com.phonehub.backend.dto.response.product.ProductListResponse;
import com.phonehub.backend.dto.response.product.ProductTemplateResponse;
import com.phonehub.backend.entity.Brand;
import com.phonehub.backend.entity.Category;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.entity.ProductImage;
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
import com.phonehub.backend.service.IProductService;
import com.phonehub.backend.service.IPromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of Product Service
 * Refactored to align with Class Diagram: Product + ProductTemplates +
 * ProductMetadata
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    // New dependencies for templates and metadata
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductMetadataRepository productMetadataRepository;
    private final ProductTemplateMapper productTemplateMapper;
    private final ProductMetadataMapper productMetadataMapper;

    // EntityManager for flushing in template updates
    private final jakarta.persistence.EntityManager entityManager;

    // PromotionService for calculating product discounts
    private final IPromotionService promotionService;

    @Override
    public List<ProductTemplateResponse> getProductMetadataGreaterThanPrice(BigDecimal price) {
        log.info("Getting product templates with price greater than: {}", price);

        List<ProductTemplate> templates = productTemplateRepository.findByPriceGreaterThan(price);

        return productTemplateMapper.toResponseList(templates);
    }

    @Override
    public ProductDetailResponse createProduct(CreateProductRequest request, Long userId) {
        log.info("Creating product with name: {} and {} templates", request.getName(), request.getTemplates().size());

        // Validate and fetch related entities
        Category category = validateAndGetCategory(request.getCategoryId());
        Brand brand = validateAndGetBrand(request.getBrandId());
        User user = validateAndGetUser(userId);

        // Check duplicate product name
        validateProductNameUnique(request.getName(), null);

        // Validate templates: Check SKU uniqueness
        validateTemplateSkuUniqueness(request.getTemplates());

        // Create product entity (base info only)
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setBrand(brand);
        product.setCreatedBy(user);
        product.setUpdatedBy(user);

        // Create product templates (variants)
        for (ProductTemplateRequest templateReq : request.getTemplates()) {
            ProductTemplate template = productTemplateMapper.toEntity(templateReq);
            template.setCreatedBy(user);
            template.setUpdatedBy(user);
            product.addTemplate(template);
        }

        // Create product metadata (technical specs) if provided
        if (request.getMetadata() != null) {
            ProductMetadata metadata = productMetadataMapper.toEntity(request.getMetadata());
            metadata.setProduct(product);
            product.setMetadata(metadata);
        }

        // Save product (cascade saves templates + metadata)
        Product savedProduct = productRepository.save(product);
        log.info("Created product with ID: {} and {} templates", savedProduct.getId(),
                savedProduct.getTemplates().size());

        return productMapper.toDetailResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        log.info("Getting product detail with ID: {}", id);

        Product product = findActiveProductById(id);

        return productMapper.toDetailResponse(product);
    }

    @Override
    public ProductDetailResponse updateProduct(Long id, UpdateProductRequest request, Long userId) {
        log.info("Updating product with ID: {}", id);

        // Find and validate product exists
        Product product = findActiveProductById(id);

        // Check duplicate name if name is being updated
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            validateProductNameUnique(request.getName(), id);
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = validateAndGetCategory(request.getCategoryId());
            product.setCategory(category);
        }

        // Update brand if provided
        if (request.getBrandId() != null) {
            Brand brand = validateAndGetBrand(request.getBrandId());
            product.setBrand(brand);
        }

        // Get user for audit
        User user = validateAndGetUser(userId);

        // Update fields using mapper (only non-null fields)
        productMapper.updateEntity(product, request);
        product.setUpdatedBy(user);

        // Update templates if provided (REPLACE all existing templates)
        if (request.getTemplates() != null && !request.getTemplates().isEmpty()) {
            log.info("Replacing all templates for product ID: {}", id);

            // Validate SKU uniqueness (except for current product's templates)
            for (ProductTemplateRequest templateReq : request.getTemplates()) {
                boolean skuExists = productTemplateRepository.existsBySku(templateReq.getSku());
                if (skuExists) {
                    // Check if SKU belongs to this product
                    ProductTemplate existingTemplate = productTemplateRepository.findBySku(templateReq.getSku())
                            .orElse(null);
                    if (existingTemplate != null && !existingTemplate.getProduct().getId().equals(id)) {
                        throw new BadRequestException("SKU đã tồn tại: " + templateReq.getSku());
                    }
                }
            }

            // Clear existing templates (orphan removal will delete them)
            product.clearTemplates();
            // Flush to avoid SKU constraint violations during re-add
            entityManager.flush();

            // Add new templates
            for (ProductTemplateRequest templateReq : request.getTemplates()) {
                ProductTemplate template = productTemplateMapper.toEntity(templateReq);
                template.setCreatedBy(user);
                template.setUpdatedBy(user);
                product.addTemplate(template);
            }
        }

        // Update metadata if provided
        if (request.getMetadata() != null) {
            log.info("Updating metadata for product ID: {}", id);
            if (product.getMetadata() != null) {
                // Update existing metadata
                productMetadataMapper.updateEntityFromRequest(request.getMetadata(), product.getMetadata());
            } else {
                // Create new metadata
                ProductMetadata metadata = productMetadataMapper.toEntity(request.getMetadata());
                metadata.setProduct(product);
                product.setMetadata(metadata);
            }
        }

        // Save updated product
        Product updatedProduct = productRepository.save(product);
        log.info("Updated product with ID: {} and {} templates", updatedProduct.getId(),
                updatedProduct.getTemplates().size());

        return productMapper.toDetailResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id, Long userId) {
        log.info("Soft deleting product with ID: {}", id);

        // Find and validate product exists
        Product product = findActiveProductById(id);

        // Get user for audit
        User user = validateAndGetUser(userId);

        // Soft delete
        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(user);

        productRepository.save(product);
        log.info("Soft deleted product with ID: {}", id);
    }

    /**
     * Increase stock for all templates of a product
     * Note: This updates ALL templates equally. For SKU-specific updates, use
     * updateProduct with templates.
     */
    @Override
    public void increaseStock(Long id, Integer amount) {
        log.info("Increasing stock for product ID: {} by {}", id, amount);

        validateStockAmount(amount, "tăng");

        Product product = findActiveProductById(id);

        validateProductHasTemplates(product);

        // Update stock for all templates
        for (ProductTemplate template : product.getTemplates()) {
            template.setStockQuantity(template.getStockQuantity() + amount);
        }

        productRepository.save(product); // Cascade saves templates

        log.info("Increased stock for product ID: {} ({} templates updated)", id, product.getTemplates().size());
    }

    /**
     * Decrease stock for all templates of a product
     * Note: This updates ALL templates equally. For SKU-specific updates, use
     * updateProduct with templates.
     */
    @Override
    public void decreaseStock(Long id, Integer amount) {
        log.info("Decreasing stock for product ID: {} by {}", id, amount);

        validateStockAmount(amount, "giảm");

        Product product = findActiveProductById(id);

        validateProductHasTemplates(product);

        // Calculate total stock across all templates
        int totalStock = product.getTemplates().stream()
                .mapToInt(ProductTemplate::getStockQuantity)
                .sum();

        if (totalStock < amount) {
            throw new BadRequestException(
                    "Số lượng trong kho không đủ. Hiện tại: " + totalStock);
        }

        // Decrease stock sequentially from templates (prioritize templates with higher
        // stock)
        // Sort templates by stock quantity descending to avoid partial deductions
        List<ProductTemplate> sortedTemplates = product.getTemplates().stream()
                .sorted(Comparator.comparingInt(ProductTemplate::getStockQuantity).reversed())
                .toList();

        int remaining = amount;
        for (ProductTemplate template : sortedTemplates) {
            if (remaining <= 0)
                break;

            int deductAmount = Math.min(template.getStockQuantity(), remaining);
            template.setStockQuantity(template.getStockQuantity() - deductAmount);
            remaining -= deductAmount;
        }

        productRepository.save(product); // Cascade saves templates

        log.info("Decreased stock for product ID: {} ({} templates updated)", id, product.getTemplates().size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            Double minPrice,
            Double maxPrice,
            Boolean status,
            Boolean includeDeleted,
            String sortBy,
            String sortDirection,
            Pageable pageable) {

        log.info(
                "Getting products - keyword: {}, categoryId: {}, brandId: {}, priceRange: [{}-{}], status: {}, includeDeleted: {}, sort: {}({})",
                keyword, categoryId, brandId, minPrice, maxPrice, status, includeDeleted, sortBy, sortDirection);

        // Convert Double to BigDecimal for repository query
        BigDecimal minPriceBD = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceBD = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        Page<Product> products;

        // Determine which repository query to use based on parameters
        if (includeDeleted != null && includeDeleted) {
            // Admin view: show all including deleted
            products = productRepository.findAllIncludingDeleted(pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Search mode
            products = productRepository.searchProducts(keyword.trim(), pageable);
        } else if (categoryId != null || brandId != null || minPriceBD != null || maxPriceBD != null) {
            // Filter mode
            products = productRepository.filterProducts(categoryId, brandId, minPriceBD, maxPriceBD, pageable);
        } else {
            // Default: get all active products
            products = productRepository.findByIsDeletedFalse(pageable);
        }

        // Map to response DTOs and enrich with template data
        List<ProductListResponse> responseList = products.stream()
                .map(product -> {
                    ProductListResponse response = productMapper.toListResponse(product);
                    enrichListResponseWithTemplateData(response, product);
                    // Set image count and images list
                    response.setImageCount(product.getImages() != null ? product.getImages().size() : 0);
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        List<ProductImageResponse> imageResponses = product.getImages().stream()
                                .sorted(Comparator.comparing(ProductImage::getImageOrder))
                                .map(img -> ProductImageResponse.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .altText(img.getAltText())
                                        .isPrimary(img.getIsPrimary())
                                        .imageOrder(img.getImageOrder())
                                        .build())
                                .toList();
                        response.setImages(imageResponses);
                    }
                    return response;
                })
                .toList();

        // Apply in-memory sorting for price and stockQuantity (since not supported at
        // DB level)
        if ("price".equals(sortBy) || "stockQuantity".equals(sortBy)) {
            responseList = responseList.stream()
                    .sorted((a, b) -> {
                        int comparison = 0;
                        if ("price".equals(sortBy)) {
                            java.math.BigDecimal priceA = a.getPrice() != null ? a.getPrice()
                                    : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal priceB = b.getPrice() != null ? b.getPrice()
                                    : java.math.BigDecimal.ZERO;
                            comparison = priceA.compareTo(priceB);
                        } else if ("stockQuantity".equals(sortBy)) {
                            Integer stockA = a.getStockQuantity() != null ? a.getStockQuantity() : 0;
                            Integer stockB = b.getStockQuantity() != null ? b.getStockQuantity() : 0;
                            comparison = stockA.compareTo(stockB);
                        }
                        return "asc".equalsIgnoreCase(sortDirection) ? comparison : -comparison;
                    })
                    .toList();
        }

        // Reconstruct Page with enriched and sorted responses
        return new org.springframework.data.domain.PageImpl<>(
                responseList,
                products.getPageable(),
                products.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getDeletedProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            Pageable pageable) {

        log.info("Getting deleted products - keyword: {}, categoryId: {}, brandId: {}",
                keyword, categoryId, brandId);

        // Query for deleted products only
        Page<Product> products = productRepository.findDeletedProducts(keyword, categoryId, brandId, pageable);

        // Map to response DTOs and enrich with template data
        List<ProductListResponse> responseList = products.stream()
                .map(product -> {
                    ProductListResponse response = productMapper.toListResponse(product);
                    enrichListResponseWithTemplateData(response, product);
                    // Set image count and images list
                    response.setImageCount(product.getImages() != null ? product.getImages().size() : 0);
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        List<ProductImageResponse> imageResponses = product.getImages().stream()
                                .sorted(Comparator.comparing(ProductImage::getImageOrder))
                                .map(img -> ProductImageResponse.builder()
                                        .id(img.getId())
                                        .imageUrl(img.getImageUrl())
                                        .altText(img.getAltText())
                                        .isPrimary(img.getIsPrimary())
                                        .imageOrder(img.getImageOrder())
                                        .build())
                                .toList();
                        response.setImages(imageResponses);
                    }
                    return response;
                })
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                responseList,
                products.getPageable(),
                products.getTotalElements());
    }

    @Override
    public void restoreProduct(Long id, Long userId) {
        log.info("Restoring product with ID: {}", id);

        Product product = productRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + id));

        if (!product.getIsDeleted()) {
            throw new BadRequestException("Sản phẩm này chưa bị xóa");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với ID: " + userId));

        product.setIsDeleted(false);
        product.setDeletedAt(null);
        product.setDeletedBy(null);
        product.setUpdatedBy(user);

        productRepository.save(product);
        log.info("Restored product with ID: {}", id);
    }

    @Override
    public void manageProductImages(Long productId, ManageImagesRequest request) {
        log.info("Managing images for product ID: {}", productId);

        // Validate product exists
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId));

        // Validate only one image is marked as primary
        long primaryCount = request.getImages().stream()
                .filter(ProductImageRequest::getIsPrimary)
                .count();

        if (primaryCount != 1) {
            throw new BadRequestException("Phải có đúng 1 ảnh được đặt làm ảnh chính");
        }

        // Validate imageOrder uniqueness and sequential
        List<Integer> orders = request.getImages().stream()
                .map(ProductImageRequest::getImageOrder)
                .sorted()
                .toList();

        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i) != i) {
                throw new BadRequestException("Thứ tự ảnh phải liên tục từ 0 đến " + (orders.size() - 1));
            }
        }

        // Delete existing images
        productImageRepository.deleteByProductId(productId);

        // Create new images
        List<ProductImage> newImages = request.getImages().stream()
                .map(imgRequest -> ProductImage.builder()
                        .product(product)
                        .imageUrl(imgRequest.getImageUrl())
                        .altText(imgRequest.getAltText())
                        .isPrimary(imgRequest.getIsPrimary())
                        .imageOrder(imgRequest.getImageOrder())
                        .build())
                .toList();

        productImageRepository.saveAll(newImages);
        log.info("Managed {} images for product ID: {}", newImages.size(), productId);
    }

    @Override
    public void deleteProductImage(Long productId, Long imageId) {
        log.info("Deleting image ID: {} for product ID: {}", imageId, productId);

        // Validate product exists
        productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId));

        // Find image
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hình ảnh với ID: " + imageId));

        // Validate image belongs to product
        if (!image.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Hình ảnh không thuộc sản phẩm này");
        }

        // Check if this is the only image
        List<ProductImage> productImages = productImageRepository.findByProductIdOrderByImageOrderAsc(productId);
        if (productImages.size() == 1) {
            throw new BadRequestException("Không thể xóa ảnh cuối cùng. Sản phẩm cần ít nhất 1 ảnh");
        }

        // Delete image
        productImageRepository.delete(image);

        // If deleted image was primary, promote the first remaining image
        if (image.getIsPrimary()) {
            List<ProductImage> remainingImages = productImageRepository.findByProductIdOrderByImageOrderAsc(productId);
            if (!remainingImages.isEmpty()) {
                ProductImage newPrimary = remainingImages.get(0);
                newPrimary.setIsPrimary(true);
                productImageRepository.save(newPrimary);
                log.info("Promoted image ID: {} as new primary for product ID: {}", newPrimary.getId(), productId);
            }
        }

        log.info("Deleted image ID: {} for product ID: {}", imageId, productId);
    }

    /**
     * Enriches ProductListResponse with price and stock calculated from templates
     * Also applies active DISCOUNT promotions automatically
     * Price = lowest price among active templates
     * Stock = sum of stockQuantity across all active templates
     * Discount = best DISCOUNT promotion applicable to this product
     */
    private void enrichListResponseWithTemplateData(ProductListResponse response, Product product) {
        List<ProductTemplate> activeTemplates = product.getTemplates().stream()
                .filter(ProductTemplate::getStatus)
                .toList();

        if (activeTemplates.isEmpty()) {
            response.setPrice(null);
            response.setStockQuantity(null);
            response.setDiscountPercent(null);
            response.setDiscountedPrice(null);
            return;
        }

        // Calculate lowest price
        BigDecimal lowestPrice = activeTemplates.stream()
                .map(ProductTemplate::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        // Calculate total stock
        int totalStock = activeTemplates.stream()
                .mapToInt(ProductTemplate::getStockQuantity)
                .sum();

        response.setPrice(lowestPrice);
        response.setStockQuantity(totalStock);

        // Calculate and apply active DISCOUNT promotions
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        Long brandId = product.getBrand() != null ? product.getBrand().getId() : null;

        Double discountPercent = promotionService.getBestDiscountForProduct(
                product.getId(),
                categoryId,
                brandId);

        if (discountPercent != null && discountPercent > 0) {
            response.setDiscountPercent(discountPercent);

            // Calculate discounted price
            BigDecimal discountMultiplier = BigDecimal.valueOf(1.0 - (discountPercent / 100.0));
            BigDecimal discountedPrice = lowestPrice.multiply(discountMultiplier)
                    .setScale(0, java.math.RoundingMode.HALF_UP); // Round to nearest integer
            response.setDiscountedPrice(discountedPrice);
        } else {
            response.setDiscountPercent(null);
            response.setDiscountedPrice(lowestPrice); // No discount, same as original price
        }
    }

    // ========== PRIVATE HELPER METHODS FOR VALIDATION ==========

    /**
     * Validate and fetch Category by ID
     */
    private Category validateAndGetCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy danh mục với ID: " + categoryId));
    }

    /**
     * Validate and fetch Brand by ID
     */
    private Brand validateAndGetBrand(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thương hiệu với ID: " + brandId));
    }

    /**
     * Validate and fetch User by ID
     */
    private User validateAndGetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với ID: " + userId));
    }

    /**
     * Find active (not deleted) product by ID
     */
    private Product findActiveProductById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + id));
    }

    /**
     * Validate product name uniqueness
     */
    private void validateProductNameUnique(String name, Long excludeId) {
        if (productRepository.existsByNameAndNotDeleted(name, excludeId)) {
            throw new BadRequestException("Sản phẩm với tên '" + name + "' đã tồn tại");
        }
    }

    /**
     * Validate template SKU uniqueness
     */
    private void validateTemplateSkuUniqueness(List<ProductTemplateRequest> templates) {
        for (ProductTemplateRequest templateReq : templates) {
            if (productTemplateRepository.existsBySku(templateReq.getSku())) {
                throw new BadRequestException("SKU đã tồn tại: " + templateReq.getSku());
            }
        }
    }

    /**
     * Validate stock amount is positive
     */
    private void validateStockAmount(Integer amount, String operation) {
        if (amount <= 0) {
            throw new BadRequestException("Số lượng " + operation + " phải lớn hơn 0");
        }
    }

    /**
     * Validate product has at least one template
     */
    private void validateProductHasTemplates(Product product) {
        if (product.getTemplates().isEmpty()) {
            throw new BadRequestException("Sản phẩm không có template nào để cập nhật stock");
        }
    }
}
