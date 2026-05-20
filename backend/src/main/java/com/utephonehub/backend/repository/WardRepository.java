package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Ward
 */
@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    /**
     * Tìm phường/xã theo mã ward_code
     * @param wardCode Mã phường/xã
     * @return Optional<Ward>
     */
    Optional<Ward> findByWardCode(String wardCode);

    /**
     * Lấy tất cả phường/xã theo mã tỉnh/thành phố
     * @param provinceCode Mã tỉnh/thành phố
     * @return List<Ward>
     */
    List<Ward> findByProvinceCodeOrderByNameAsc(String provinceCode);

    /**
     * Lấy tất cả phường/xã, sắp xếp theo tên
     * @return List<Ward>
     */
    List<Ward> findAllByOrderByNameAsc();

    /**
     * Kiểm tra tồn tại theo mã ward_code
     * @param wardCode Mã phường/xã
     * @return boolean
     */
    boolean existsByWardCode(String wardCode);

    /**
     * Đếm số phường/xã theo mã tỉnh
     * @param provinceCode Mã tỉnh/thành phố
     * @return long
     */
    long countByProvinceCode(String provinceCode);
}
