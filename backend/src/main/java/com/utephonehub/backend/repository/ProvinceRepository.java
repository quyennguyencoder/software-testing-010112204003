package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Province
 */
@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    /**
     * Tìm tỉnh/thành phố theo mã province_code
     * @param provinceCode Mã tỉnh/thành phố
     * @return Optional<Province>
     */
    Optional<Province> findByProvinceCode(String provinceCode);

    /**
     * Lấy tất cả tỉnh/thành phố, sắp xếp theo tên
     * @return List<Province>
     */
    List<Province> findAllByOrderByNameAsc();

    /**
     * Kiểm tra tồn tại theo mã province_code
     * @param provinceCode Mã tỉnh/thành phố
     * @return boolean
     */
    boolean existsByProvinceCode(String provinceCode);
}
