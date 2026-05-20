package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.request.product.ProductMetadataRequest;
import com.utephonehub.backend.dto.response.product.ProductMetadataResponse;
import com.utephonehub.backend.entity.ProductMetadata;
import org.mapstruct.*;

/**
 * MapStruct mapper for ProductMetadata entity
 * Converts between Entity and DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMetadataMapper {

    /**
     * Convert Request DTO to Entity
     * Product relationship is set manually in Service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProductMetadata toEntity(ProductMetadataRequest request);

    /**
     * Convert Entity to Response DTO
     */
    ProductMetadataResponse toResponse(ProductMetadata entity);

    /**
     * Update existing entity from Request DTO
     * Null values in request will be ignored (won't overwrite existing values)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ProductMetadataRequest request, @MappingTarget ProductMetadata entity);
}
