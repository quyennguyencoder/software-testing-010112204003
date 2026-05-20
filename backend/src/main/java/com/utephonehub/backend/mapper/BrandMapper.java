package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.brand.BrandResponse;
import com.utephonehub.backend.entity.Brand;
import org.mapstruct.*;

/**
 * MapStruct mapper for Brand entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    
    BrandResponse toResponse(Brand brand);
}
