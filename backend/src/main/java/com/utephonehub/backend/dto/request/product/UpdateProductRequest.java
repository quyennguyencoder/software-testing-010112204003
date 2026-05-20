package com.utephonehub.backend.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating existing Product
 * All fields are optional - only provided fields will be updated
 * Aligned with Class Diagram: Can update Product info, Templates, and Metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    /**
     * Core product information (optional updates)
     */
    @Size(min = 5, max = 200, message = "Tên sản phẩm phải từ 5-200 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @Size(max = 255, message = "URL hình ảnh không được vượt quá 255 ký tự")
    private String thumbnailUrl;

    private Long categoryId;

    private Long brandId;

    private Boolean status;

    /**
     * Product variants (templates)
     * 
     * ⚠️ WARNING: If provided, will REPLACE ALL existing templates (destructive operation)
     * This means all old templates will be deleted and replaced with the new list.
     * To add/update/delete single template, use dedicated template management API instead.
     * 
     * Use this only when you need to completely rebuild the product's variant structure.
     */
    @Valid
    @Size(min = 1, message = "Nếu cập nhật templates, phải có ít nhất 1 biến thể")
    private List<ProductTemplateRequest> templates;

    /**
     * Technical specifications (metadata)
     * If provided, will update metadata fields
     */
    @Valid
    private ProductMetadataRequest metadata;
}
