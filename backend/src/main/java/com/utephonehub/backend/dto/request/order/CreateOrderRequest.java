package com.utephonehub.backend.dto.request.order;

import com.utephonehub.backend.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)")
    private String phoneNumber;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
    
    private java.math.BigDecimal shippingFee; // Phí vận chuyển (Optional)
    
    private String shippingUnit; // Đơn vị vận chuyển (Optional, VD: "GHN", "GHTK")
    
    private String note; // Ghi chú đơn hàng (Optional)
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
    
    private String promotionId; // UUID của promotion (DISCOUNT/VOUCHER)
    
    private String freeshippingPromotionId; // UUID của freeship promotion (Optional)
    
    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    private List<OrderItemRequest> items;
}
