package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng wards - Phường/Xã
 */
@Entity
@Table(name = "wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã phường/xã (unique)
     * VD: "00070", "00073"
     */
    @Column(name = "ward_code", nullable = false, length = 6, unique = true)
    private String wardCode;

    /**
     * Tên phường/xã
     * VD: "Phường Hoàn Kiếm", "Phường Cửa Nam"
     */
    @Column(nullable = false)
    private String name;

    /**
     * Mã tỉnh/thành phố mà phường/xã thuộc về
     * Foreign key tới bảng provinces
     * VD: "01", "79"
     */
    @Column(name = "province_code", nullable = false, length = 3)
    private String provinceCode;
}
