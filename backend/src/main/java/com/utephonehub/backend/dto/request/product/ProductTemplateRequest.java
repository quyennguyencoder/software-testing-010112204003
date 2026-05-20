package com.utephonehub.backend.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating ProductTemplate (product variant)
 * Aligned with usecase M02: SKU, color, storage, RAM, price, stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTemplateRequest {

    /**
     * Stock Keeping Unit - Unique identifier
     * Format: PRODUCT_CODE-STORAGE-COLOR (e.g., IP15PM-256-BLK, ip15pm-256-blk)
     */
    @NotBlank(message = "SKU không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9-_]{3,50}$", message = "SKU phải là chữ cái, số, dấu gạch ngang hoặc gạch dưới (3-50 ký tự)")
    private String sku;

    /**
     * Variant color (e.g., "Titan Black", "Natural Titanium")
     */
    @Size(max = 50, message = "Màu sắc tối đa 50 ký tự")
    private String color;

    /**
     * Storage capacity (e.g., "256GB", "512GB", "1TB")
     */
    @Size(max = 50, message = "Dung lượng tối đa 50 ký tự")
    private String storage;

    /**
     * RAM capacity (e.g., "8GB", "16GB") - mainly for laptops
     */
    @Size(max = 50, message = "RAM tối đa 50 ký tự")
    private String ram;

    /**
     * Price for this variant
     */
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Digits(integer = 15, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal price;

    /**
     * Stock quantity for this variant
     */
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải >= 0")
    private Integer stockQuantity;

    /**
     * Template active status
     */
    @Builder.Default
    private Boolean status = true;
}
