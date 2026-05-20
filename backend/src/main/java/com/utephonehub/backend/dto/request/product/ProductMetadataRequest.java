package com.utephonehub.backend.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating ProductMetadata (technical specifications)
 * Aligned with usecase M02: screen, CPU, camera, battery, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetadataRequest {

    // ==================== PRICING ====================
    
    @DecimalMin(value = "0.0", message = "Giá nhập phải >= 0")
    private BigDecimal importPrice;

    @DecimalMin(value = "0.0", message = "Giá bán phải >= 0")
    private BigDecimal salePrice;

    // ==================== DISPLAY (Smartphone/Tablet) ====================
    
    @Size(max = 100, message = "Độ phân giải màn hình tối đa 100 ký tự")
    private String screenResolution; // "2796 x 1290"

    @DecimalMin(value = "0.0", message = "Kích thước màn hình phải > 0")
    @DecimalMax(value = "50.0", message = "Kích thước màn hình phải < 50 inch")
    private Double screenSize; // 6.7 inches

    @Size(max = 100, message = "Công nghệ màn hình tối đa 100 ký tự")
    private String screenTechnology; // "Super Retina XDR OLED"

    @Min(value = 30, message = "Tần số quét phải >= 30Hz")
    @Max(value = 240, message = "Tần số quét phải <= 240Hz")
    private Integer refreshRate; // 120Hz

    // ==================== PROCESSOR ====================
    
    @Size(max = 100, message = "CPU/Chipset tối đa 100 ký tự")
    private String cpuChipset; // "Apple A17 Pro"

    @Size(max = 100, message = "GPU tối đa 100 ký tự")
    private String gpu; // "Apple GPU 6-core"

    // ==================== CAMERA ====================
    
    @DecimalMin(value = "0.1", message = "Camera phải >= 0.1 MP")
    @DecimalMax(value = "200.0", message = "Camera phải <= 200 MP")
    private Double cameraMegapixels; // 48.0 MP

    @Size(max = 200, message = "Chi tiết camera tối đa 200 ký tự")
    private String cameraDetails; // "48MP + 12MP ultra + 12MP telephoto"

    @DecimalMin(value = "0.1", message = "Camera trước phải >= 0.1 MP")
    @DecimalMax(value = "100.0", message = "Camera trước phải <= 100 MP")
    private Double frontCameraMegapixels; // 12.0 MP

    // ==================== BATTERY ====================
    
    @Min(value = 100, message = "Dung lượng pin phải >= 100 mAh")
    @Max(value = 100000, message = "Dung lượng pin phải <= 100000 mAh")
    private Integer batteryCapacity; // 4422 mAh

    @Min(value = 5, message = "Công suất sạc phải >= 5W")
    @Max(value = 200, message = "Công suất sạc phải <= 200W")
    private Integer chargingPower; // 20W

    @Size(max = 100, message = "Loại sạc tối đa 100 ký tự")
    private String chargingType; // "USB-C PD", "MagSafe"

    // ==================== PHYSICAL ====================
    
    @DecimalMin(value = "1.0", message = "Trọng lượng phải >= 1g")
    @DecimalMax(value = "10000.0", message = "Trọng lượng phải <= 10000g")
    private Double weight; // 221.0 grams

    @Size(max = 100, message = "Kích thước tối đa 100 ký tự")
    private String dimensions; // "159.9 x 76.7 x 8.25 mm"

    @Size(max = 100, message = "Chất liệu tối đa 100 ký tự")
    private String material; // "Titanium", "Aluminum"

    // ==================== LAPTOP-SPECIFIC ====================
    
    @Size(max = 100, message = "Hệ điều hành tối đa 100 ký tự")
    private String operatingSystem; // "macOS Sonoma"

    @Size(max = 100, message = "Loại bàn phím tối đa 100 ký tự")
    private String keyboardType; // "Magic Keyboard"

    @Size(max = 200, message = "Cổng kết nối tối đa 200 ký tự")
    private String ports; // "3x Thunderbolt 4, HDMI 2.1"

    // ==================== SMARTWATCH-SPECIFIC ====================
    
    @Size(max = 50, message = "Kích thước vỏ tối đa 50 ký tự")
    private String caseSize; // "45mm"

    @Size(max = 200, message = "Tính năng sức khỏe tối đa 200 ký tự")
    private String healthFeatures; // "ECG, Blood Oxygen"

    @Min(value = 1, message = "Thời lượng pin phải >= 1 ngày")
    @Max(value = 365, message = "Thời lượng pin phải <= 365 ngày")
    private Integer batteryLifeDays; // 1 day

    // ==================== CONNECTIVITY ====================
    
    @Size(max = 100, message = "Kết nối không dây tối đa 100 ký tự")
    private String wirelessConnectivity; // "5G, Wi-Fi 6E, Bluetooth 5.3"

    @Size(max = 100, message = "Loại SIM tối đa 100 ký tự")
    private String simType; // "Dual SIM (nano-SIM + eSIM)"

    // ==================== OTHER FEATURES ====================
    
    @Size(max = 200, message = "Chống nước tối đa 200 ký tự")
    private String waterResistance; // "IP68"

    @Size(max = 200, message = "Âm thanh tối đa 200 ký tự")
    private String audioFeatures; // "Stereo speakers, Dolby Atmos"

    @Size(max = 200, message = "Bảo mật tối đa 200 ký tự")
    private String securityFeatures; // "Face ID"

    @Size(max = 2000, message = "Thông số bổ sung tối đa 2000 ký tự")
    private String additionalSpecs; // Free text or JSON
}
