
package com.utephonehub.backend.dto.response.order;

import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đơn hàng công khai (không nhạy cảm)")
public class PublicOrderTrackingResponse {
    
    @Schema(description = "Mã đơn hàng", example = "ORD-001")
    private String orderCode;
    
    @Schema(description = "Trạng thái đơn hàng", example = "DELIVERED")
    private OrderStatus status;
    
    @Schema(description = "Tên trạng thái hiển thị", example = "Đã giao hàng")
    private String statusDisplay;
    
    @Schema(description = "Mô tả trạng thái", example = "Đơn hàng đã được giao thành công")
    private String statusDescription;
    
    @Schema(description = "Tên người nhận", example = "Trần Thị Hương")
    private String recipientName;
    
    @Schema(description = "Số điện thoại người nhận (ẩn một phần)", example = "091****678")
    private String maskedPhoneNumber;
    
    @Schema(description = "Tổng tiền", example = "32990000. 00")
    private BigDecimal totalAmount;
    
    @Schema(description = "Phương thức thanh toán", example = "COD")
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Đơn vị vận chuyển", example = "GHN")
    private String shippingUnit;
    
    @Schema(description = "Ngày tạo đơn hàng", example = "2025-12-09T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Ngày cập nhật gần nhất", example = "2025-12-11T14:20:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Có thể hủy đơn hay không", example = "false")
    private boolean canCancel;
    
    @Schema(description = "Các hành động khả thi", example = "[\"view_status\", \"contact_support\"]")
    private String[] availableActions;
    
    @Schema(description = "Thông tin hướng dẫn cho khách hàng")
    private String customerMessage;
    
    public static PublicOrderTrackingResponse fromEntity(Order order) {
        return PublicOrderTrackingResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusDisplay(getStatusDisplayName(order.getStatus()))
                .statusDescription(getStatusDescription(order.getStatus()))
                .recipientName(order.getRecipientName())
                .maskedPhoneNumber(maskPhoneNumber(order.getPhoneNumber()))
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .shippingUnit(order.getShippingUnit())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .canCancel(order.getStatus() == OrderStatus.PENDING)
                .availableActions(getAvailableActions(order.getStatus()))
                .customerMessage(getCustomerMessage(order.getStatus()))
                .build();
    }
    
    private static String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELLED -> "Đã hủy";
        };
    }
    
    private static String getStatusDescription(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đơn hàng đang chờ cửa hàng xác nhận.  Thời gian xử lý: 1-2 giờ làm việc. ";
            case CONFIRMED -> "Đơn hàng đã được xác nhận và đang chuẩn bị hàng hóa.";
            case SHIPPING -> "Đơn hàng đang được giao đến địa chỉ của bạn.";
            case DELIVERED -> "Đơn hàng đã được giao thành công.  Cảm ơn bạn đã mua hàng! ";
            case CANCELLED -> "Đơn hàng đã bị hủy. ";
        };
    }
    
    private static String[] getAvailableActions(OrderStatus status) {
        return switch (status) {
            case PENDING -> new String[]{"view_status", "cancel_order", "contact_support"};
            case CONFIRMED, SHIPPING -> new String[]{"view_status", "contact_support"};
            case DELIVERED -> new String[]{"view_status", "review_product", "contact_support"};
            case CANCELLED -> new String[]{"view_status", "reorder", "contact_support"};
        };
    }
    
    private static String getCustomerMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đơn hàng của bạn đang được xử lý. Bạn có thể hủy đơn nếu cần.";
            case CONFIRMED -> "Đơn hàng đã được xác nhận. Chúng tôi đang chuẩn bị hàng cho bạn.";
            case SHIPPING -> "Đơn hàng đang trên đường giao đến bạn. Vui lòng chú ý điện thoại.";
            case DELIVERED -> "Đơn hàng đã giao thành công. Hãy đánh giá sản phẩm nhé!";
            case CANCELLED -> "Đơn hàng đã được hủy. Bạn có thể đặt lại đơn hàng mới.";
        };
    }
    
    private static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) {
            return "***";
        }
        
        // Ẩn giữa số điện thoại, chỉ hiện 3 số đầu và 3 số cuối
        // VD: 0912345678 -> 091****678
        String start = phoneNumber.substring(0, 3);
        String end = phoneNumber.substring(phoneNumber. length() - 3);
        return start + "****" + end;
    }
}