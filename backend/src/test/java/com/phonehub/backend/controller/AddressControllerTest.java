package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.address.AddressRequest;
import com.phonehub.backend.dto.response.address.AddressResponse;
import com.phonehub.backend.service.intf.IAddressService;
import com.phonehub.backend.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAddressService addressService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AddressController addressController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
    }

    @Test
    public void getAddresses_ShouldReturnAddressList() throws Exception {
        Long userId = 1L;
        AddressResponse addr1 = AddressResponse.builder().id(1L).recipientName("John Doe").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();
        AddressResponse addr2 = AddressResponse.builder().id(2L).recipientName("Jane Doe").phoneNumber("0912345678").streetAddress("456 Avenue").ward("Ward 2").province("Province B").isDefault(false).build();
        List<AddressResponse> addresses = Arrays.asList(addr1, addr2);

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(addressService.getUserAddresses(userId)).thenReturn(addresses);

        mockMvc.perform(get("/api/v1/user/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].recipientName").value("John Doe"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].recipientName").value("Jane Doe"));
    }

    @Test
    public void createAddress_ShouldReturnCreatedAddress() throws Exception {
        Long userId = 1L;
        AddressRequest request = AddressRequest.builder().recipientName("John Doe").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();
        AddressResponse response = AddressResponse.builder().id(1L).recipientName("John Doe").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(addressService.createAddress(eq(userId), any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/user/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thêm địa chỉ thành công"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    public void updateAddress_ShouldReturnUpdatedAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 1L;
        AddressRequest request = AddressRequest.builder().recipientName("John Doe Updated").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();
        AddressResponse response = AddressResponse.builder().id(addressId).recipientName("John Doe Updated").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(addressService.updateAddress(eq(userId), eq(addressId), any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/user/addresses/{id}", addressId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật địa chỉ thành công"))
                .andExpect(jsonPath("$.data.recipientName").value("John Doe Updated"));
    }

    @Test
    public void deleteAddress_ShouldReturnSuccess() throws Exception {
        Long userId = 1L;
        Long addressId = 1L;

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        doNothing().when(addressService).deleteAddress(userId, addressId);

        mockMvc.perform(delete("/api/v1/user/addresses/{id}", addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa địa chỉ thành công"));
    }

    @Test
    public void setDefaultAddress_ShouldReturnUpdatedAddress() throws Exception {
        Long userId = 1L;
        Long addressId = 1L;
        AddressResponse response = AddressResponse.builder().id(addressId).recipientName("John Doe").phoneNumber("0987654321").streetAddress("123 Street").ward("Ward 1").province("Province A").isDefault(true).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(addressService.setDefaultAddress(userId, addressId)).thenReturn(response);

        mockMvc.perform(put("/api/v1/user/addresses/{id}/set-default", addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đặt địa chỉ mặc định thành công"))
                .andExpect(jsonPath("$.data.isDefault").value(true));
    }
}
