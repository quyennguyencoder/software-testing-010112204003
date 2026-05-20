package com.utephonehub.backend.dto.response.brand;

import java.time.LocalDateTime;

import com.utephonehub.backend.entity.Brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponse {

    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private Long productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Brand entity to BrandResponse DTO
     * @param brand Brand entity
     * @return BrandResponse
     */
    public static BrandResponse fromEntity(Brand brand) {
        if (brand == null) {
            return null;
        }

        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .productCount(0L) // Default to 0, will be set by service
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .build();
    }
}

