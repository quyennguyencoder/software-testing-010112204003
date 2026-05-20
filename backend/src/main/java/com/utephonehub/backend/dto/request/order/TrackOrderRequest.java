
package com.utephonehub.backend.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints. Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tra cứu đơn hàng công khai")
public class TrackOrderRequest {
    
    @NotBlank(message = "Mã đơn hàng không được để trống")
    @Size(min = 3, max = 20, message = "Mã đơn hàng phải từ 3-20 ký tự")
    @Schema(description = "Mã đơn hàng", example = "ORD-001", required = true)
    private String orderCode;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    @Schema(description = "Email khách hàng đặt hàng", example = "huong.tran@gmail. com", required = true)
    private String email;
}