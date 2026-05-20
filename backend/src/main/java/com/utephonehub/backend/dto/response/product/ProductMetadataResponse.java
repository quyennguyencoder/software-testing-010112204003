package com.utephonehub.backend.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for ProductMetadata response
 * Used in ProductDetailResponse to show technical specifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetadataResponse {

    // Pricing
    private BigDecimal importPrice;
    private BigDecimal salePrice;

    // Display (Smartphone/Tablet)
    private String screenResolution;
    private Double screenSize;
    private String screenTechnology;
    private Integer refreshRate;

    // Processor
    private String cpuChipset;
    private String gpu;

    // Camera
    private Double cameraMegapixels;
    private String cameraDetails;
    private Double frontCameraMegapixels;

    // Battery
    private Integer batteryCapacity;
    private Integer chargingPower;
    private String chargingType;

    // Physical
    private Double weight;
    private String dimensions;
    private String material;

    // Laptop-specific
    private String operatingSystem;
    private String keyboardType;
    private String ports;

    // Smartwatch-specific
    private String caseSize;
    private String healthFeatures;
    private Integer batteryLifeDays;

    // Connectivity
    private String wirelessConnectivity;
    private String simType;

    // Other features
    private String waterResistance;
    private String audioFeatures;
    private String securityFeatures;
    private String additionalSpecs;
}
