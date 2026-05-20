package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.response.location.ProvinceResponse;
import com.utephonehub.backend.dto.response.location.WardResponse;
import com.utephonehub.backend.service.ILocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Location API
 * Quản lý địa chỉ hành chính Việt Nam (Tỉnh/Thành phố, Phường/Xã)
 * 
 * API này cung cấp dữ liệu địa chỉ từ database local, không phụ thuộc external API
 * Được sử dụng cho form nhập địa chỉ của người dùng (đặt hàng, profile, etc.)
 */
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location", description = "API quản lý địa chỉ hành chính Việt Nam")
public class LocationController {

    private final ILocationService locationService;

    /**
     * GET /api/v1/locations/provinces
     * Lấy danh sách tất cả tỉnh/thành phố Việt Nam
     * 
     * Use case: Load dropdown tỉnh/thành phố trong form địa chỉ
     * Response: 63 tỉnh/thành phố, sắp xếp A-Z
     */
    @GetMapping("/provinces")
    @Operation(
            summary = "Lấy danh sách tỉnh/thành phố",
            description = "Trả về danh sách tất cả tỉnh/thành phố Việt Nam, sắp xếp theo tên"
    )
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> getAllProvinces() {
        List<ProvinceResponse> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tỉnh/thành phố thành công", provinces));
    }

    /**
     * GET /api/v1/locations/provinces/{provinceCode}
     * Lấy chi tiết một tỉnh/thành phố theo mã province_code
     * 
     * @param provinceCode Mã tỉnh (VD: "01" = Hà Nội, "79" = TP.HCM)
     * Use case: Hiển thị thông tin chi tiết tỉnh đã chọn
     */
    @GetMapping("/provinces/{provinceCode}")
    @Operation(
            summary = "Lấy chi tiết tỉnh/thành phố",
            description = "Trả về thông tin chi tiết của một tỉnh/thành phố theo mã province_code"
    )
    public ResponseEntity<ApiResponse<ProvinceResponse>> getProvinceByCode(
            @Parameter(description = "Mã tỉnh/thành phố (VD: 01, 79, 48)")
            @PathVariable String provinceCode) {
        ProvinceResponse province = locationService.getProvinceByCode(provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết tỉnh/thành phố thành công", province));
    }

    /**
     * GET /api/v1/locations/wards
     * Lấy danh sách TẤT CẢ phường/xã trong cả nước
     * 
     * ⚠️ WARNING: Response có thể rất lớn (3000+ records)
     * Use case: Admin import data, analytics
     * Không nên dùng cho form người dùng (dùng endpoint /provinces/{code}/wards)
     */
    @GetMapping("/wards")
    @Operation(
            summary = "Lấy danh sách tất cả phường/xã",
            description = "Trả về danh sách tất cả phường/xã Việt Nam, sắp xếp theo tên"
    )
    public ResponseEntity<ApiResponse<List<WardResponse>>> getAllWards() {
        List<WardResponse> wards = locationService.getAllWards();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phường/xã thành công", wards));
    }

    /**
     * GET /api/v1/locations/provinces/{provinceCode}/wards
     * Lấy danh sách phường/xã theo tỉnh/thành phố
     * 
     * @param provinceCode Mã tỉnh/thành phố
     * Use case: Load dropdown phường/xã sau khi user chọn tỉnh
     * Flow: User chọn tỉnh → Call API này → Hiển thị danh sách phường/xã
     */
    @GetMapping("/provinces/{provinceCode}/wards")
    @Operation(
            summary = "Lấy danh sách phường/xã theo tỉnh",
            description = "Trả về danh sách tất cả phường/xã thuộc một tỉnh/thành phố, sắp xếp theo tên"
    )
    public ResponseEntity<ApiResponse<List<WardResponse>>> getWardsByProvinceCode(
            @Parameter(description = "Mã tỉnh/thành phố (VD: 01, 79, 48)")
            @PathVariable String provinceCode) {
        List<WardResponse> wards = locationService.getWardsByProvinceCode(provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phường/xã theo tỉnh thành công", wards));
    }

    /**
     * GET /api/v1/locations/wards/{wardCode}
     * Lấy chi tiết một phường/xã theo mã ward_code
     * 
     * @param wardCode Mã phường/xã (VD: "00070" = Phường Hoàn Kiếm)
     * Use case: Hiển thị thông tin chi tiết phường/xã đã lưu
     */
    @GetMapping("/wards/{wardCode}")
    @Operation(
            summary = "Lấy chi tiết phường/xã",
            description = "Trả về thông tin chi tiết của một phường/xã theo mã ward_code"
    )
    public ResponseEntity<ApiResponse<WardResponse>> getWardByCode(
            @Parameter(description = "Mã phường/xã (VD: 00070, 00073)")
            @PathVariable String wardCode) {
        WardResponse ward = locationService.getWardByCode(wardCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phường/xã thành công", ward));
    }

    /**
     * GET /api/v1/locations/provinces/{provinceCode}/validate
     * Kiểm tra mã tỉnh/thành phố có hợp lệ không
     * 
     * @param provinceCode Mã tỉnh cần kiểm tra
     * @return true nếu tồn tại, false nếu không
     * Use case: Validation form địa chỉ, kiểm tra data integrity
     */
    @GetMapping("/provinces/{provinceCode}/validate")
    @Operation(
            summary = "Kiểm tra mã tỉnh hợp lệ",
            description = "Kiểm tra xem mã tỉnh/thành phố có tồn tại trong hệ thống hay không"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateProvinceCode(
            @Parameter(description = "Mã tỉnh/thành phố cần kiểm tra")
            @PathVariable String provinceCode) {
        boolean isValid = locationService.isValidProvinceCode(provinceCode);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra mã tỉnh thành công", isValid));
    }

    /**
     * GET /api/v1/locations/wards/{wardCode}/validate
     * Kiểm tra mã phường/xã có hợp lệ không
     */
    @GetMapping("/wards/{wardCode}/validate")
    @Operation(
            summary = "Kiểm tra mã phường/xã hợp lệ",
            description = "Kiểm tra xem mã phường/xã có tồn tại trong hệ thống hay không"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateWardCode(
            @Parameter(description = "Mã phường/xã cần kiểm tra")
            @PathVariable String wardCode) {
        boolean isValid = locationService.isValidWardCode(wardCode);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra mã phường/xã thành công", isValid));
    }
}
