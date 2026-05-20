package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.request.product.ProductTemplateRequest;
import com.utephonehub.backend.dto.response.product.ProductTemplateResponse;
import com.utephonehub.backend.entity.ProductTemplate;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for ProductTemplate entity
 * Converts between Entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductTemplateMapper {

    /**
     * Convert Request DTO to Entity
     * Product relationship is set manually in Service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "stockStatus", ignore = true) // Auto-calculated in @PrePersist
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductTemplate toEntity(ProductTemplateRequest request);

    /**
     * Convert Entity to Response DTO
     */
    ProductTemplateResponse toResponse(ProductTemplate entity);

    /**
     * Convert list of entities to list of responses
     */
    List<ProductTemplateResponse> toResponseList(List<ProductTemplate> entities);

    /**
     * Update existing entity from Request DTO
     * Null values in request will be ignored (won't overwrite existing values)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "stockStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(ProductTemplateRequest request, @MappingTarget ProductTemplate entity);
}
