package com.utephonehub.backend.dto.response.location;

import lombok.*;

/**
 * DTO Response cho Province - Tỉnh/Thành phố
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceResponse {

    /**
     * ID tỉnh/thành phố
     */
    private Long id;

    /**
     * Mã tỉnh/thành phố
     * VD: "01", "79", "48"
     */
    private String provinceCode;

    /**
     * Tên tỉnh/thành phố
     * VD: "Thành phố Hà Nội", "Thành phố Hồ Chí Minh"
     */
    private String name;

    /**
     * Loại hành chính
     * VD: "Thành phố Trung Ương", "Tỉnh"
     */
    private String placeType;

    /**
     * Mã quốc gia
     * VD: "VN"
     */
    private String country;
}
