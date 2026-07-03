package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.address.AddressRequest;
import com.phonehub.backend.dto.response.address.AddressResponse;
import com.phonehub.backend.entity.Address;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.mapper.AddressMapper;
import com.phonehub.backend.repository.AddressRepository;
import com.phonehub.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setUser(testUser);
        testAddress.setIsDefault(false);

        addressRequest = new AddressRequest();
        addressRequest.setRecipientName("Test Recipient");
        addressRequest.setPhoneNumber("0123456789");
        addressRequest.setStreetAddress("123 Test St");
        addressRequest.setIsDefault(true);
    }

    @Test
    void getUserAddresses_Success() {
        when(addressRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testAddress));
        AddressResponse response = new AddressResponse();
        when(addressMapper.toResponse(testAddress)).thenReturn(response);

        List<AddressResponse> result = addressService.getUserAddresses(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createAddress_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        
        AddressResponse response = new AddressResponse();
        when(addressMapper.toResponse(testAddress)).thenReturn(response);

        AddressResponse result = addressService.createAddress(1L, addressRequest);

        assertNotNull(result);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void createAddress_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.createAddress(99L, addressRequest));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void updateAddress_Success() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserIdAndIsDefaultTrue(1L)).thenReturn(Optional.of(new Address()));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        
        AddressResponse response = new AddressResponse();
        when(addressMapper.toResponse(testAddress)).thenReturn(response);

        AddressResponse result = addressService.updateAddress(1L, 1L, addressRequest);

        assertNotNull(result);
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void deleteAddress_Success() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAddress));

        addressService.deleteAddress(1L, 1L);

        verify(addressRepository, times(1)).delete(testAddress);
    }

    @Test
    void setDefaultAddress_Success() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserIdAndIsDefaultTrue(1L)).thenReturn(Optional.of(new Address()));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        
        AddressResponse response = new AddressResponse();
        when(addressMapper.toResponse(testAddress)).thenReturn(response);

        AddressResponse result = addressService.setDefaultAddress(1L, 1L);

        assertNotNull(result);
        verify(addressRepository, times(2)).save(any(Address.class));
    }
}
