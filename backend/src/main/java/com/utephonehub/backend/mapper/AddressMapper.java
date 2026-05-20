package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.request.address.AddressRequest;
import com.utephonehub.backend.dto.response.address.AddressResponse;
import com.utephonehub.backend.entity.Address;
import org.mapstruct.*;

/**
 * MapStruct mapper for Address entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {
    
    /**
     * Convert Address entity to AddressResponse DTO
     * @param address Address entity
     * @return AddressResponse DTO
     */
    AddressResponse toResponse(Address address);
    
    /**
     * Convert AddressRequest to Address entity
     * @param request AddressRequest DTO
     * @return Address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toEntity(AddressRequest request);
    
    /**
     * Update Address entity from AddressRequest
     * @param request AddressRequest DTO
     * @param address Address entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(AddressRequest request, @MappingTarget Address address);
}
