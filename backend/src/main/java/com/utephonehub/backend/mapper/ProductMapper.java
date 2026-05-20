package com.utephonehub.backend.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.utephonehub.backend.dto.request.product.CreateProductRequest;
import com.utephonehub.backend.dto.request.product.UpdateProductRequest;
import com.utephonehub.backend.dto.response.product.ProductDetailResponse;
import com.utephonehub.backend.dto.response.product.ProductListResponse;
import com.utephonehub.backend.dto.response.product.ProductResponse;
import com.utephonehub.backend.entity.Product;

/**
 * MapStruct mapper for Product entity and DTOs
 * Updated to handle ProductTemplate and ProductMetadata relationships
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMapper.class, BrandMapper.class, ProductImageMapper.class, 
                ProductTemplateMapper.class, ProductMetadataMapper.class})
public interface ProductMapper {
    
    /**
     * Convert Product entity to ProductResponse DTO
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    ProductResponse toResponse(Product product);
    
    /**
     * Convert Product entity to ProductListResponse DTO (simplified)
     * Price and stockQuantity will be calculated in Service layer
     */
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "price", ignore = true) // Calculated from cheapest template
    @Mapping(target = "stockQuantity", ignore = true) // Sum of all templates
    ProductListResponse toListResponse(Product product);
    
    /**
     * Convert Product entity to ProductDetailResponse DTO (full details)
     * Includes templates and metadata
     */
    @Mapping(target = "category", source = "category")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "templates", source = "templates")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "createdByUsername", source = "createdBy.username")
    @Mapping(target = "updatedByUsername", source = "updatedBy.username")
    ProductDetailResponse toDetailResponse(Product product);
    
    /**
     * Convert list of Products to list of ProductListResponses
     */
    List<ProductListResponse> toListResponses(List<Product> products);
    
    /**
     * Convert CreateProductRequest to Product entity (base info only)
     * Templates and metadata are handled separately in Service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "templates", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toEntity(CreateProductRequest request);
    
    /**
     * Update existing Product entity with UpdateProductRequest data
     * Only updates base product info, not templates/metadata
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "templates", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEntity(@MappingTarget Product product, UpdateProductRequest request);
}
