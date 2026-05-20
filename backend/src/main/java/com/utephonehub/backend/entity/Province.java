package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng provinces - Tỉnh/Thành phố
 */
@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mã tỉnh/thành phố (unique)
     * VD: "01", "79", "48"
     */
    @Column(name = "province_code", nullable = false, length = 3, unique = true)
    private String provinceCode;

    /**
     * Tên tỉnh/thành phố
     * VD: "Thành phố Hà Nội", "Thành phố Hồ Chí Minh"
     */
    @Column(nullable = false)
    private String name;

    /**
     * Loại hành chính
     * VD: "Thành phố Trung Ương", "Tỉnh"
     */
    @Column(name = "place_type", nullable = false)
    private String placeType;

    /**
     * Mã quốc gia
     * VD: "VN"
     */
    @Column(nullable = false, length = 10)
    private String country;
}
