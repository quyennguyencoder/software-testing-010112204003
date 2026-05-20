package com.utephonehub.backend.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating new Product
 * Aligned with Class Diagram: Product + ProductTemplates + ProductMetadata
 * Aligned with Usecase M02: Requires name, category, brand, at least 1 template (with SKU, price, stock)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    /**
     * Core product information
     */
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 5, max = 200, message = "Tên sản phẩm phải từ 5-200 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @Size(max = 255, message = "URL hình ảnh không được vượt quá 255 ký tự")
    private String thumbnailUrl;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;

    @Builder.Default
    private Boolean status = true;

    /**
     * Product variants (templates)
     * According to Usecase M02: Must have at least 1 variant with SKU, price, stock
     * Each variant represents a specific configuration (e.g., iPhone 256GB Black, iPhone 512GB White)
     */
    @NotEmpty(message = "Phải có ít nhất 1 biến thể sản phẩm (SKU, giá, tồn kho)")
    @Valid
    @Builder.Default
    private List<ProductTemplateRequest> templates = new ArrayList<>();

    /**
     * Technical specifications (metadata)
     * Optional: Can be added later via update
     */
    @Valid
    private ProductMetadataRequest metadata;

    /**
     * Product images (optional)
     * According to Usecase M02: Max 10 images, JPG/PNG/WebP, max 5MB each
     */
    @Valid
    @Size(max = 10, message = "Tối đa 10 ảnh sản phẩm")
    @Builder.Default
    private List<ProductImageRequest> images = new ArrayList<>();
}
