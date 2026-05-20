package com.phonehub.backend.mapper;

import com.phonehub.backend.dto.response.category.CategoryResponse;
import com.phonehub.backend.entity.Category;
import org.mapstruct.*;

/**
 * MapStruct mapper for Category entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryResponse toResponse(Category category);
}
