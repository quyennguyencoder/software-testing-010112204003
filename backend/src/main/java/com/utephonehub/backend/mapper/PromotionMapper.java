package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.PromotionResponse;
import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.PromotionTarget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
/**
 * Mapper for converting Promotion entities to DTOs
 * Follows Single Responsibility Principle (SRP) - only handles mapping
 * Follows DRY principle - centralized mapping logic
 */
@Mapper(componentModel = "spring")
public interface PromotionMapper {
    @Mappings({
        @Mapping(source = "template.id", target = "templateId"),
        @Mapping(source = "template.code", target = "templateCode"),
        @Mapping(source = "template.type", target = "templateType"),
        @Mapping(source = "targets", target = "targets")
    })
    PromotionResponse toResponse(Promotion promotion);
    List<PromotionResponse> toResponseList(List<Promotion> promotions);
    @Mapping(source = "applicableObjectId", target = "applicableObjectId")
    PromotionResponse.TargetResponse mapTarget(PromotionTarget target);
    List<PromotionResponse.TargetResponse> mapTargets(List<PromotionTarget> targets);
}
