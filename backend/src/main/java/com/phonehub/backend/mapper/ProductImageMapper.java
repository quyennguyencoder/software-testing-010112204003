package com.phonehub.backend.mapper;

import com.phonehub.backend.dto.response.product.ProductImageResponse;
import com.phonehub.backend.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for ProductImage entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {
    
    ProductImageResponse toResponse(ProductImage image);
    
    List<ProductImageResponse> toResponses(List<ProductImage> images);
}
