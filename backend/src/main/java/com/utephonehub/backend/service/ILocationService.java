package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.response.location.ProvinceResponse;
import com.utephonehub.backend.dto.response.location.WardResponse;

import java.util.List;

/**
 * Interface for Location Service - Quản lý địa chỉ hành chính Việt Nam
 */
public interface ILocationService {

    /**
     * Lấy danh sách tất cả tỉnh/thành phố
     * @return List<ProvinceResponse>
     */
    List<ProvinceResponse> getAllProvinces();

    /**
     * Lấy chi tiết một tỉnh/thành phố theo mã province_code
     * @param provinceCode Mã tỉnh/thành phố
     * @return ProvinceResponse
     */
    ProvinceResponse getProvinceByCode(String provinceCode);

    /**
     * Lấy danh sách tất cả phường/xã
     * @return List<WardResponse>
     */
    List<WardResponse> getAllWards();

    /**
     * Lấy danh sách phường/xã theo mã tỉnh/thành phố
     * @param provinceCode Mã tỉnh/thành phố
     * @return List<WardResponse>
     */
    List<WardResponse> getWardsByProvinceCode(String provinceCode);

    /**
     * Lấy chi tiết một phường/xã theo mã ward_code
     * @param wardCode Mã phường/xã
     * @return WardResponse
     */
    WardResponse getWardByCode(String wardCode);

    /**
     * Kiểm tra mã tỉnh/thành phố có tồn tại không
     * @param provinceCode Mã tỉnh/thành phố
     * @return boolean
     */
    boolean isValidProvinceCode(String provinceCode);

    /**
     * Kiểm tra mã phường/xã có tồn tại không
     * @param wardCode Mã phường/xã
     * @return boolean
     */
    boolean isValidWardCode(String wardCode);
}
