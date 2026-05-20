package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.brand.CreateBrandRequest;
import com.utephonehub.backend.dto.request.brand.UpdateBrandRequest;
import com.utephonehub.backend.dto.response.brand.BrandResponse;

import java.util.List;

/**
 * Interface for Brand Service operations
 */
public interface IBrandService {

    /**
     * Get all brands
     * @return List of BrandResponse
     */
    List<BrandResponse> getAllBrands();

    /**
     * Get brand by ID
     * @param id Brand ID
     * @return BrandResponse
     */
    BrandResponse getBrandById(Long id);

    /**
     * Create new brand
     * @param request CreateBrandRequest
     * @return BrandResponse
     */
    BrandResponse createBrand(CreateBrandRequest request);

    /**
     * Update existing brand
     * @param id Brand ID
     * @param request UpdateBrandRequest
     * @return BrandResponse
     */
    BrandResponse updateBrand(Long id, UpdateBrandRequest request);

    /**
     * Delete brand by ID
     * Check constraints before deletion:
     * - Cannot delete if brand has products linked
     * @param id Brand ID
     */
    void deleteBrand(Long id);
}
