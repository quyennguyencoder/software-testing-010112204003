package com.phonehub.backend.mapper;

import com.phonehub.backend.dto.response.brand.BrandResponse;
import com.phonehub.backend.entity.Brand;
import org.mapstruct.*;

/**
 * MapStruct mapper for Brand entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    
    BrandResponse toResponse(Brand brand);
}
