package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.address.AddressRequest;
import com.utephonehub.backend.dto.response.address.AddressResponse;

import java.util.List;

/**
 * Interface for Address Service operations
 */
public interface IAddressService {
    
    /**
     * Get all addresses for a user
     * @param userId User ID
     * @return List of AddressResponse
     */
    List<AddressResponse> getUserAddresses(Long userId);
    
    /**
     * Create new address
     * @param userId User ID
     * @param request Address request
     * @return Created AddressResponse
     */
    AddressResponse createAddress(Long userId, AddressRequest request);
    
    /**
     * Update existing address
     * @param userId User ID
     * @param addressId Address ID
     * @param request Address request
     * @return Updated AddressResponse
     */
    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);
    
    /**
     * Delete address
     * @param userId User ID
     * @param addressId Address ID
     */
    void deleteAddress(Long userId, Long addressId);
    
    /**
     * Set address as default
     * @param userId User ID
     * @param addressId Address ID
     * @return Updated AddressResponse
     */
    AddressResponse setDefaultAddress(Long userId, Long addressId);
}
