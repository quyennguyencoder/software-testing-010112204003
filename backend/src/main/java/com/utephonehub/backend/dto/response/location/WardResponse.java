package com.utephonehub.backend.dto.response.location;

import lombok.*;

/**
 * DTO Response cho Ward - Phường/Xã
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardResponse {

    /**
     * ID phường/xã
     */
    private Long id;

    /**
     * Mã phường/xã
     * VD: "00070", "00073"
     */
    private String wardCode;

    /**
     * Tên phường/xã
     * VD: "Phường Hoàn Kiếm", "Phường Cửa Nam"
     */
    private String name;

    /**
     * Mã tỉnh/thành phố mà phường/xã thuộc về
     * VD: "01", "79"
     */
    private String provinceCode;
}
