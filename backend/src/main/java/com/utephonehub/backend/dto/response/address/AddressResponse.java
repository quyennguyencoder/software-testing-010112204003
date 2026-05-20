package com.utephonehub.backend.dto.response.address;

import com.utephonehub.backend.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String streetAddress;
    private String ward;
    private String wardCode;
    private String province;
    private String provinceCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse fromEntity(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .streetAddress(address.getStreetAddress())
                .ward(address.getWard())
                .wardCode(address.getWardCode())
                .province(address.getProvince())
                .provinceCode(address.getProvinceCode())
                .isDefault(address.getIsDefault())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}

