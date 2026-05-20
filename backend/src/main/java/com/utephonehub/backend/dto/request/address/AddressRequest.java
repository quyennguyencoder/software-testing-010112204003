package com.utephonehub.backend.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(min = 2, max = 100, message = "Tên người nhận phải từ 2 đến 100 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 15, message = "Số điện thoại phải từ 10 đến 15 ký tự")
    private String phoneNumber;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String streetAddress;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    private String wardCode;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;

    private String provinceCode;

    private Boolean isDefault = false;
}

