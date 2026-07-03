package com.phonehub.backend.controller;

import com.phonehub.backend.dto.response.location.ProvinceResponse;
import com.phonehub.backend.dto.response.location.WardResponse;
import com.phonehub.backend.service.intf.ILocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ILocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @Test
    public void getAllProvinces_ShouldReturnProvincesList() throws Exception {
        ProvinceResponse province = new ProvinceResponse();
        province.setProvinceCode("01");
        province.setName("Hà Nội");

        when(locationService.getAllProvinces()).thenReturn(Collections.singletonList(province));

        mockMvc.perform(get("/api/v1/locations/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy danh sách tỉnh/thành phố thành công"));
    }

    @Test
    public void getProvinceByCode_ShouldReturnProvince() throws Exception {
        String code = "01";
        ProvinceResponse province = new ProvinceResponse();
        province.setProvinceCode(code);
        province.setName("Hà Nội");

        when(locationService.getProvinceByCode(code)).thenReturn(province);

        mockMvc.perform(get("/api/v1/locations/provinces/{provinceCode}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provinceCode").value(code));
    }

    @Test
    public void getAllWards_ShouldReturnWardsList() throws Exception {
        WardResponse ward = new WardResponse();
        ward.setWardCode("00001");
        ward.setName("Phường Phúc Xá");

        when(locationService.getAllWards()).thenReturn(Collections.singletonList(ward));

        mockMvc.perform(get("/api/v1/locations/wards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getWardsByProvinceCode_ShouldReturnWardsList() throws Exception {
        String provinceCode = "01";
        WardResponse ward = new WardResponse();
        ward.setWardCode("00001");
        ward.setName("Phường Phúc Xá");

        when(locationService.getWardsByProvinceCode(provinceCode)).thenReturn(Collections.singletonList(ward));

        mockMvc.perform(get("/api/v1/locations/provinces/{provinceCode}/wards", provinceCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getWardByCode_ShouldReturnWard() throws Exception {
        String code = "00001";
        WardResponse ward = new WardResponse();
        ward.setWardCode(code);
        ward.setName("Phường Phúc Xá");

        when(locationService.getWardByCode(code)).thenReturn(ward);

        mockMvc.perform(get("/api/v1/locations/wards/{wardCode}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wardCode").value(code));
    }

    @Test
    public void validateProvinceCode_ShouldReturnValidationResult() throws Exception {
        String code = "01";
        when(locationService.isValidProvinceCode(code)).thenReturn(true);

        mockMvc.perform(get("/api/v1/locations/provinces/{provinceCode}/validate", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void validateWardCode_ShouldReturnValidationResult() throws Exception {
        String code = "00001";
        when(locationService.isValidWardCode(code)).thenReturn(true);

        mockMvc.perform(get("/api/v1/locations/wards/{wardCode}/validate", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }
}
