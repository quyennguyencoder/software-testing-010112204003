package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductMetadata Entity - Technical specifications for products
 * Aligned with Class Diagram: Product 1-1 ProductMetadata
 * 
 * Stores detailed technical specs that vary by product type:
 * - Smartphones: screen, camera, battery, CPU
 * - Laptops: CPU, RAM, GPU, keyboard type
 * - Smartwatches: case size, health features
 */
@Entity
@Table(name = "product_metadata", indexes = {
    @Index(name = "idx_product_metadata_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-One relationship with Product
     * Each product has exactly one metadata record
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    // ==================== PRICING INFO ====================
    
    /**
     * Import price from supplier (cost price)
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal importPrice;

    /**
     * Retail sale price (may differ from template price during promotions)
     */
    @Column(precision = 15, scale = 2)
    private BigDecimal salePrice;

    // ==================== DISPLAY SPECS (Smartphone/Tablet) ====================
    
    @Column(length = 100)
    private String screenResolution; // "2796 x 1290", "2532 x 1170"

    @Column
    @DecimalMin(value = "1.0", message = "Kích thước màn hình phải >= 1.0 inch")
    @DecimalMax(value = "50.0", message = "Kích thước màn hình phải <= 50.0 inch")
    private Double screenSize; // 6.7, 6.1, 10.9 (inches)

    @Column(length = 100)
    private String screenTechnology; // "Super Retina XDR OLED", "AMOLED"

    @Column
    private Integer refreshRate; // 60, 90, 120 (Hz)

    // ==================== PROCESSOR & MEMORY ====================
    
    @Column(length = 100)
    private String cpuChipset; // "Apple A17 Pro", "Snapdragon 8 Gen 3"

    @Column(length = 100)
    private String gpu; // "Apple GPU 6-core", "Adreno 750"

    // ==================== CAMERA SPECS ====================
    
    @Column
    private Double cameraMegapixels; // 48.0, 108.0 (main camera MP)

    @Column(length = 200)
    private String cameraDetails; // "48MP main + 12MP ultra + 12MP telephoto"

    @Column
    private Double frontCameraMegapixels; // 12.0, 32.0

    // ==================== BATTERY & CHARGING ====================
    
    @Column
    private Integer batteryCapacity; // 4422, 5000 (mAh)

    @Column
    private Integer chargingPower; // 20, 30, 67 (W)

    @Column(length = 100)
    private String chargingType; // "USB-C PD", "MagSafe", "Qi Wireless"

    // ==================== PHYSICAL ATTRIBUTES ====================
    
    @Column
    private Double weight; // 221.0, 195.5 (grams)

    @Column(length = 100)
    private String dimensions; // "159.9 x 76.7 x 8.25 mm"

    @Column(length = 100)
    private String material; // "Titanium", "Aluminum", "Glass"

    // ==================== LAPTOP-SPECIFIC ====================
    
    @Column(length = 100)
    private String operatingSystem; // "macOS Sonoma", "Windows 11 Pro"

    @Column(length = 100)
    private String keyboardType; // "Magic Keyboard", "Backlit Chiclet"

    @Column(length = 200)
    private String ports; // "3x Thunderbolt 4, HDMI 2.1, SD card"

    // ==================== SMARTWATCH-SPECIFIC ====================
    
    @Column(length = 50)
    private String caseSize; // "45mm", "41mm"

    @Column(length = 200)
    private String healthFeatures; // "ECG, Blood Oxygen, Heart Rate Monitor"

    @Column
    private Integer batteryLifeDays; // 18, 36 (hours -> days)

    // ==================== CONNECTIVITY ====================
    
    @Column(length = 100)
    private String wirelessConnectivity; // "5G, Wi-Fi 6E, Bluetooth 5.3"

    @Column(length = 100)
    private String simType; // "Dual SIM (nano-SIM + eSIM)"

    // ==================== OTHER FEATURES ====================
    
    @Column(length = 200)
    private String waterResistance; // "IP68 (6m for 30 min)"

    @Column(length = 200)
    private String audioFeatures; // "Stereo speakers, Dolby Atmos"

    @Column(length = 200)
    private String securityFeatures; // "Face ID", "Fingerprint sensor"

    // ==================== ADDITIONAL INFO ====================
    
    @Column(columnDefinition = "TEXT")
    private String additionalSpecs; // JSON or free-text for any other specs

    // Audit fields
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
