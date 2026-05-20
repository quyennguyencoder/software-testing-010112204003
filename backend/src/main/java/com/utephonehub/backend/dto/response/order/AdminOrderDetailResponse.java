
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
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin order detail response")
public class AdminOrderDetailResponse {

	@Schema(description = "Order ID", example = "1")
	private Long id;

	@Schema(description = "Order code", example = "ORD-001")
	private String orderCode;

	// Customer Information
	@Schema(description = "Customer ID", example = "2")
	private Long customerId;

	@Schema(description = "Customer name", example = "Trần Thị Hương")
	private String customerName;

	@Schema(description = "Customer email", example = "huong.tran@gmail.com")
	private String customerEmail;

	@Schema(description = "Customer phone", example = "0912345678")
	private String customerPhone;

	// Recipient Information
	@Schema(description = "Recipient name", example = "Trần Thị Hương")
	private String recipientName;

	@Schema(description = "Recipient phone", example = "0912345678")
	private String recipientPhone;

	@Schema(description = "Full shipping address", example = "123 Lê Lợi, Phường Bến Thành, TP. Hồ Chí Minh")
	private String shippingAddress;

	// Order Details
	@Schema(description = "Order status", example = "DELIVERED")
	private OrderStatus status;

	@Schema(description = "Status display name", example = "Đã giao hàng")
	private String statusDisplay;

	@Schema(description = "Payment method", example = "COD")
	private PaymentMethod paymentMethod;

	@Schema(description = "Total amount", example = "32990000.00")
	private BigDecimal totalAmount;

	@Schema(description = "Shipping fee", example = "30000.00")
	private BigDecimal shippingFee;

	@Schema(description = "Shipping unit", example = "GHN")
	private String shippingUnit;

	@Schema(description = "Order note", example = "Giao hàng giờ hành chính")
	private String note;

	// Items - Using inline OrderItem DTO
	@Schema(description = "Order items")
	private List<OrderItemDto> items;

	// Timestamps
	@Schema(description = "Order created date", example = "2025-12-09T10:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Order last updated date", example = "2025-12-11T14:20:00")
	private LocalDateTime updatedAt;

	// Admin specific fields
	@Schema(description = "Available status transitions for admin", example = "[\"CONFIRMED\", \"CANCELLED\"]")
	private List<OrderStatus> availableStatusTransitions;

	@Schema(description = "Admin notes/comments", example = "Customer confirmed delivery")
	private String adminNotes;

	// ✅ NESTED DTO CLASS FOR ORDER ITEMS
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Order item information")
	public static class OrderItemDto {

		@Schema(description = "Order item ID", example = "1")
		private Long id;

		@Schema(description = "Product ID", example = "1")
		private Long productId;

		@Schema(description = "Product name", example = "iPhone 15 Pro Max")
		private String productName;

		@Schema(description = "Product thumbnail", example = "https://example.com/iphone15.jpg")
		private String productThumbnail;

		@Schema(description = "Quantity", example = "2")
		private Integer quantity;

		@Schema(description = "Unit price", example = "32990000.00")
		private BigDecimal price;

		@Schema(description = "Total price (quantity * price)", example = "65980000.00")
		private BigDecimal totalPrice;

		@Schema(description = "Created date", example = "2025-12-09T10:30:00")
		private LocalDateTime createdAt;
	}

	// ✅ STATIC METHOD WITH MANUAL MAPPING
	public static AdminOrderDetailResponse fromEntity(Order order) {
		return AdminOrderDetailResponse.builder().id(order.getId()).orderCode(order.getOrderCode())
				.customerId(order.getUser() != null ? order.getUser().getId() : null)
				.customerName(order.getUser() != null ? order.getUser().getFullName() : "Guest")
				.customerEmail(order.getEmail())
				.customerPhone(order.getUser() != null ? order.getUser().getPhoneNumber() : null)
				.recipientName(order.getRecipientName()).recipientPhone(order.getPhoneNumber())
				.shippingAddress(order.getShippingAddress()).status(order.getStatus())
				.statusDisplay(getStatusDisplayName(order.getStatus())).paymentMethod(order.getPaymentMethod())
				.totalAmount(order.getTotalAmount()).shippingFee(order.getShippingFee())
				.shippingUnit(order.getShippingUnit()).note(order.getNote())

				.items(order.getItems() != null ? order.getItems().stream().map(item -> {
					return OrderItemDto.builder().id(item.getId())
							.productId(item.getProduct() != null ? item.getProduct().getId() : null)
							.productName(item.getProduct() != null ? item.getProduct().getName() : "Unknown Product")
							.productThumbnail(item.getProduct() != null ? item.getProduct().getThumbnailUrl() : null)
							.quantity(item.getQuantity()).price(item.getPrice())
							.totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
							.createdAt(item.getCreatedAt()).build();
				}).collect(Collectors.toList()) : List.of()).createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt())
				.availableStatusTransitions(getAvailableStatusTransitions(order.getStatus())).adminNotes("") // Can be
																												// enhanced
																												// later
				.build();
	}

	// ✅ HELPER METHODS
	private static String getStatusDisplayName(OrderStatus status) {
		return switch (status) {
		case PENDING -> "Chờ xác nhận";
		case CONFIRMED -> "Đã xác nhận";
		case SHIPPING -> "Đang giao hàng";
		case DELIVERED -> "Đã giao hàng";
		case CANCELLED -> "Đã hủy";
		};
	}

	private static List<OrderStatus> getAvailableStatusTransitions(OrderStatus currentStatus) {
		return switch (currentStatus) {
		case PENDING -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
		case CONFIRMED -> List.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
		case SHIPPING -> List.of(OrderStatus.DELIVERED);
		case DELIVERED, CANCELLED -> List.of(); // No further transitions
		};
	}
}