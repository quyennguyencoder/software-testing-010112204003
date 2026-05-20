package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.request.auth.RegisterRequest;
import com.utephonehub.backend.dto.response.user.UserResponse;
import com.utephonehub.backend.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    /**
     * Convert User entity to UserResponse DTO
     * @param user User entity
     * @return UserResponse DTO
     */
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    UserResponse toResponse(User user);
    
    /**
     * Convert RegisterRequest to User entity
     * @param request RegisterRequest DTO
     * @return User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "cart", ignore = true)
    User toEntity(RegisterRequest request);
}
