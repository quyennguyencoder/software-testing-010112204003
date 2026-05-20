package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.request.PromotionTemplateRequest;
import com.utephonehub.backend.dto.response.PromotionTemplateResponse;
import com.utephonehub.backend.entity.PromotionTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for PromotionTemplate entity
 */
@Mapper(componentModel = "spring")
public interface PromotionTemplateMapper {

    /**
     * Map PromotionTemplate entity to response DTO
     */
    PromotionTemplateResponse toResponse(PromotionTemplate template);

    /**
     * Map request DTO to PromotionTemplate entity
     * Note: createdAt should be set manually in service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    PromotionTemplate toEntity(PromotionTemplateRequest request);

    /**
     * Update existing entity from request DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    void updateEntity(PromotionTemplateRequest request, @MappingTarget PromotionTemplate template);
}
