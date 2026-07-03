package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.response.location.ProvinceResponse;
import com.phonehub.backend.dto.response.location.WardResponse;
import com.phonehub.backend.entity.Province;
import com.phonehub.backend.entity.Ward;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.ProvinceRepository;
import com.phonehub.backend.repository.WardRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceImplTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Province testProvince;
    private Ward testWard;

    @BeforeEach
    void setUp() {
        testProvince = new Province();
        testProvince.setId(1L);
        testProvince.setProvinceCode("01");
        testProvince.setName("Hà Nội");

        testWard = new Ward();
        testWard.setId(1L);
        testWard.setWardCode("00001");
        testWard.setName("Phúc Xá");
        testWard.setProvinceCode("01");
    }

    @Test
    void getAllProvinces_Success() {
        when(provinceRepository.findAllByOrderByNameAsc()).thenReturn(Collections.singletonList(testProvince));

        List<ProvinceResponse> result = locationService.getAllProvinces();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("01", result.get(0).getProvinceCode());
        assertEquals("Hà Nội", result.get(0).getName());
    }

    @Test
    void getProvinceByCode_Success() {
        when(provinceRepository.findByProvinceCode("01")).thenReturn(Optional.of(testProvince));

        ProvinceResponse result = locationService.getProvinceByCode("01");

        assertNotNull(result);
        assertEquals("01", result.getProvinceCode());
    }

    @Test
    void getProvinceByCode_NotFound_ThrowsException() {
        when(provinceRepository.findByProvinceCode("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationService.getProvinceByCode("99"));
    }

    @Test
    void getAllWards_Success() {
        when(wardRepository.findAllByOrderByNameAsc()).thenReturn(Collections.singletonList(testWard));

        List<WardResponse> result = locationService.getAllWards();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("00001", result.get(0).getWardCode());
    }

    @Test
    void getWardsByProvinceCode_Success() {
        when(provinceRepository.existsByProvinceCode("01")).thenReturn(true);
        when(wardRepository.findByProvinceCodeOrderByNameAsc("01")).thenReturn(Collections.singletonList(testWard));

        List<WardResponse> result = locationService.getWardsByProvinceCode("01");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getWardsByProvinceCode_ProvinceNotFound_ThrowsException() {
        when(provinceRepository.existsByProvinceCode("99")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> locationService.getWardsByProvinceCode("99"));
    }

    @Test
    void getWardByCode_Success() {
        when(wardRepository.findByWardCode("00001")).thenReturn(Optional.of(testWard));

        WardResponse result = locationService.getWardByCode("00001");

        assertNotNull(result);
        assertEquals("00001", result.getWardCode());
    }

    @Test
    void getWardByCode_NotFound_ThrowsException() {
        when(wardRepository.findByWardCode("99999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> locationService.getWardByCode("99999"));
    }

    @Test
    void isValidProvinceCode_ReturnsTrue() {
        when(provinceRepository.existsByProvinceCode("01")).thenReturn(true);
        assertTrue(locationService.isValidProvinceCode("01"));
    }

    @Test
    void isValidWardCode_ReturnsTrue() {
        when(wardRepository.existsByWardCode("00001")).thenReturn(true);
        assertTrue(locationService.isValidWardCode("00001"));
    }
}
