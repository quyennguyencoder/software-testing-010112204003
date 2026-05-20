package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.location.ProvinceResponse;
import com.utephonehub.backend.dto.response.location.WardResponse;
import com.utephonehub.backend.entity.Province;
import com.utephonehub.backend.entity.Ward;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.ProvinceRepository;
import com.utephonehub.backend.repository.WardRepository;
import com.utephonehub.backend.service.ILocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ILocationService
 * Service xử lý logic nghiệp vụ cho Location API
 * 
 * Dữ liệu lấy từ database local (không gọi external API)
 * Performance: Query đơn giản, có index, response time < 50ms
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Read-only transaction cho tất cả methods
public class LocationServiceImpl implements ILocationService {

    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    /**
     * Lấy danh sách tất cả tỉnh/thành phố
     * 
     * @return List 63 tỉnh/thành phố, sắp xếp theo tên A-Z
     * Performance: ~20ms, có index trên name column
     */
    @Override
    public List<ProvinceResponse> getAllProvinces() {
        log.info("Getting all provinces");
        List<Province> provinces = provinceRepository.findAllByOrderByNameAsc();
        return provinces.stream()
                .map(this::mapToProvinceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết tỉnh/thành phố theo mã
     * 
     * @param provinceCode Mã tỉnh (VD: "01", "79")
     * @return ProvinceResponse
     * @throws ResourceNotFoundException nếu không tìm thấy
     */
    @Override
    public ProvinceResponse getProvinceByCode(String provinceCode) {
        log.info("Getting province by code: {}", provinceCode);
        Province province = provinceRepository.findByProvinceCode(provinceCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tỉnh/thành phố với mã: " + provinceCode));
        return mapToProvinceResponse(province);
    }

    /**
     * Lấy TẤT CẢ phường/xã trong cả nước
     * 
     * ⚠️ WARNING: Trả về 3000+ records, response time ~500ms
     * Nên cache kết quả hoặc pagination nếu dùng cho production
     * 
     * @return List tất cả phường/xã, sắp xếp A-Z
     */
    @Override
    public List<WardResponse> getAllWards() {
        log.info("Getting all wards");
        List<Ward> wards = wardRepository.findAllByOrderByNameAsc();
        return wards.stream()
                .map(this::mapToWardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách phường/xã theo tỉnh/thành phố
     * 
     * Business rules:
     * - Kiểm tra tỉnh có tồn tại trước (throw exception nếu không)
     * - Chỉ trả về phường/xã thuộc tỉnh đó
     * - Sắp xếp theo tên A-Z
     * 
     * @param provinceCode Mã tỉnh/thành phố
     * @return List phường/xã của tỉnh (có thể empty nếu tỉnh không có dữ liệu)
     * @throws ResourceNotFoundException nếu tỉnh không tồn tại
     */
    @Override
    public List<WardResponse> getWardsByProvinceCode(String provinceCode) {
        log.info("Getting wards by province code: {}", provinceCode);
        
        // Validate: Kiểm tra tỉnh có tồn tại không
        if (!provinceRepository.existsByProvinceCode(provinceCode)) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy tỉnh/thành phố với mã: " + provinceCode);
        }
        
        // Query wards theo province_code, có index nên nhanh
        List<Ward> wards = wardRepository.findByProvinceCodeOrderByNameAsc(provinceCode);
        return wards.stream()
                .map(this::mapToWardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết phường/xã theo mã
     * 
     * @param wardCode Mã phường/xã (VD: "00070")
     * @return WardResponse
     * @throws ResourceNotFoundException nếu không tìm thấy
     */
    @Override
    public WardResponse getWardByCode(String wardCode) {
        log.info("Getting ward by code: {}", wardCode);
        Ward ward = wardRepository.findByWardCode(wardCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phường/xã với mã: " + wardCode));
        return mapToWardResponse(ward);
    }

    /**
     * Kiểm tra mã tỉnh có hợp lệ không
     * 
     * @param provinceCode Mã tỉnh cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     * Use case: Validation form, check data integrity
     */
    @Override
    public boolean isValidProvinceCode(String provinceCode) {
        return provinceRepository.existsByProvinceCode(provinceCode);
    }

    /**
     * Kiểm tra mã phường/xã có hợp lệ không
     * 
     * @param wardCode Mã phường/xã cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     */
    @Override
    public boolean isValidWardCode(String wardCode) {
        return wardRepository.existsByWardCode(wardCode);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Map Province entity sang ProvinceResponse DTO
     * Chuyển đổi từ entity (database layer) sang DTO (API layer)
     */
    private ProvinceResponse mapToProvinceResponse(Province province) {
        return ProvinceResponse.builder()
                .id(province.getId())
                .provinceCode(province.getProvinceCode())
                .name(province.getName())
                .placeType(province.getPlaceType())
                .country(province.getCountry())
                .build();
    }

    /**
     * Map Ward entity sang WardResponse DTO
     * Chuyển đổi từ entity (database layer) sang DTO (API layer)
     */
    private WardResponse mapToWardResponse(Ward ward) {
        return WardResponse.builder()
                .id(ward.getId())
                .wardCode(ward.getWardCode())
                .name(ward.getName())
                .provinceCode(ward.getProvinceCode())
                .build();
    }
}
