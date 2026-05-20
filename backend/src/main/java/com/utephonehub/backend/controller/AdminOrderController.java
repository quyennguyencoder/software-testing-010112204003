// src/main/java/com/utephonehub/backend/controller/AdminOrderController.java
package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.order.AdminOrderFilterRequest;
import com.utephonehub.backend.dto.response.order.AdminOrderDetailResponse;
import com.utephonehub.backend.dto.response.order.AdminOrderListResponse;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import com.utephonehub.backend.service.IAdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Order Management", description = "APIs quản lý đơn hàng cho Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

	private final IAdminOrderService adminOrderService;

	/**
	 * GET /api/v1/admin/orders Get all orders with filtering and pagination
	 */
	@GetMapping
	@Operation(summary = "🔍 Lấy danh sách đơn hàng linh hoạt (Admin)", description = "Lấy danh sách tất cả đơn hàng với khả năng filter và phân trang linh hoạt.   "
			+ "🎯 **Tham số nào để trống sẽ bị BỎ QUA** - không ảnh hưởng đến kết quả tìm kiếm.\n\n"
			+ "**Ví dụ sử dụng:**\n" + "- Không có fromDate/toDate = tìm kiếm trong tất cả thời gian\n"
			+ "- Không có status = hiển thị tất cả trạng thái đơn hàng\n"
			+ "- Không có search = không lọc theo từ khóa\n"
			+ "- minAmount = 0 hoặc trống = không giới hạn số tiền tối thiểu\n\n"
			+ "**Chỉ Admin mới có quyền truy cập được.**")
	public ResponseEntity<ApiResponse<Page<AdminOrderListResponse>>> getAllOrders(

			@Parameter(description = "🔤 Search keyword (order code, email, phone, recipient name) - **Để trống = không filter theo từ khóa**") @RequestParam(required = false) String search,

			@Parameter(description = "📊 Filter by order status - **Để trống = tất cả trạng thái**") @RequestParam(required = false) OrderStatus status,

			@Parameter(description = "💳 Filter by payment method - **Để trống = tất cả phương thức thanh toán**") @RequestParam(required = false) String paymentMethod,

			@Parameter(description = "👤 Filter by customer ID - **Để trống = tất cả khách hàng**") @RequestParam(required = false) Long customerId,

			@Parameter(description = "📧 Filter by customer email - **Để trống = không lọc theo email**") @RequestParam(required = false) String customerEmail,

			@Parameter(description = "📅 Filter from date (YYYY-MM-DD) - **Để trống = tìm từ ngày đầu tiên**") @RequestParam(required = false) String fromDate,

			@Parameter(description = "📅 Filter to date (YYYY-MM-DD) - **Để trống = tìm đến hiện tại**") @RequestParam(required = false) String toDate,

			@Parameter(description = "💰 Minimum amount - **Để trống hoặc 0 = không giới hạn tối thiểu**") @RequestParam(required = false) String minAmount,

			@Parameter(description = "💰 Maximum amount - **Để trống hoặc 0 = không giới hạn tối đa**") @RequestParam(required = false) String maxAmount,

			@Parameter(description = "📄 Page number (0-based)") @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "📏 Page size") @RequestParam(defaultValue = "20") int size,

			@Parameter(description = "🔀 Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,

			@Parameter(description = "⬆️⬇️ Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

		log.info(
				"🔍 Admin FLEXIBLE search - page:  {}, size: {}, search: '{}', status: {}, fromDate: '{}', toDate: '{}'",
				page, size, search, status, fromDate, toDate);

		// ✅ BUILD FLEXIBLE FILTER REQUEST
		// Build filter request with enhanced error handling
		AdminOrderFilterRequest.AdminOrderFilterRequestBuilder filterBuilder = AdminOrderFilterRequest.builder()
				.search(search) // Will be normalized in service layer
				.status(status).customerId(customerId).customerEmail(customerEmail);

		// ✅ ENHANCED DATE PARSING WITH DETAILED ERROR HANDLING
		// Handle date parsing (enhanced with better error messages)
		try {
			if (fromDate != null && !fromDate.trim().isEmpty()) {
				LocalDate parsedFromDate = LocalDate.parse(fromDate.trim());
				filterBuilder.fromDate(parsedFromDate);
				log.debug("✅ FromDate parsed successfully: {}", parsedFromDate);
			} else {
				log.debug("ℹ️ No fromDate provided - will search from beginning of time");
			}

			if (toDate != null && !toDate.trim().isEmpty()) {
				LocalDate parsedToDate = LocalDate.parse(toDate.trim());
				filterBuilder.toDate(parsedToDate);
				log.debug("✅ ToDate parsed successfully: {}", parsedToDate);
			} else {
				log.debug("ℹ️ No toDate provided - will search until current time");
			}

		} catch (DateTimeParseException e) {
			log.warn("❌ Invalid date format provided: fromDate='{}', toDate='{}' - Error: {}", fromDate, toDate,
					e.getMessage());
			return ResponseEntity.badRequest().body(ApiResponse.error(400, // ✅ statusCode trước
					String.format("❌ Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD. "
							+ "FromDate: '%s', ToDate:  '%s'", fromDate, toDate)));
		}

		// ✅ ENHANCED AMOUNT PARSING WITH DETAILED ERROR HANDLING
		// Handle amount parsing with better validation
		try {
			if (minAmount != null && !minAmount.trim().isEmpty()) {
				BigDecimal parsedMinAmount = new BigDecimal(minAmount.trim());
				if (parsedMinAmount.compareTo(BigDecimal.ZERO) > 0) {
					filterBuilder.minAmount(parsedMinAmount);
					log.debug("✅ MinAmount parsed successfully:  {}", parsedMinAmount);
				} else {
					log.debug("ℹ️ MinAmount is zero or negative - will ignore this filter");
				}
			} else {
				log.debug("ℹ️ No minAmount provided - no minimum amount limit");
			}

			if (maxAmount != null && !maxAmount.trim().isEmpty()) {
				BigDecimal parsedMaxAmount = new BigDecimal(maxAmount.trim());
				if (parsedMaxAmount.compareTo(BigDecimal.ZERO) > 0) {
					filterBuilder.maxAmount(parsedMaxAmount);
					log.debug("✅ MaxAmount parsed successfully: {}", parsedMaxAmount);
				} else {
					log.debug("ℹ️ MaxAmount is zero or negative - will ignore this filter");
				}
			} else {
				log.debug("ℹ️ No maxAmount provided - no maximum amount limit");
			}

		} catch (NumberFormatException e) {
			log.warn("❌ Invalid amount format provided:  minAmount='{}', maxAmount='{}' - Error: {}", minAmount,
					maxAmount, e.getMessage());
			return ResponseEntity.badRequest().body(ApiResponse.error(400, // ✅ statusCode trước
					String.format("❌ Định dạng số tiền không hợp lệ.  Vui lòng nhập số hợp lệ. "
							+ "MinAmount: '%s', MaxAmount: '%s'", minAmount, maxAmount)));
		}

		// ✅ ENHANCED PAYMENT METHOD PARSING
		// Parse payment method safely with detailed error handling
		try {
			if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
				PaymentMethod parsedPaymentMethod = PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
				filterBuilder.paymentMethod(parsedPaymentMethod);
				log.debug("✅ PaymentMethod parsed successfully: {}", parsedPaymentMethod);
			} else {
				log.debug("ℹ️ No paymentMethod provided - will search all payment methods");
			}
		} catch (IllegalArgumentException e) {
			log.warn("❌ Invalid payment method provided: '{}' - Available values: {}", paymentMethod,
					java.util.Arrays.toString(PaymentMethod.values()));
			return ResponseEntity.badRequest().body(ApiResponse.error(400, // ✅ statusCode trước
					String.format("❌ Phương thức thanh toán không hợp lệ:  '%s'. " + "Các giá trị hợp lệ:  %s",
							paymentMethod, java.util.Arrays.toString(PaymentMethod.values()))));
		}

		// Build final filter request
		AdminOrderFilterRequest filterRequest = filterBuilder.build();

		// Create pageable
		Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		// ✅ EXECUTE FLEXIBLE SEARCH WITH IMPROVED SERVICE METHOD
		// Get orders using the enhanced flexible search
		Page<AdminOrderListResponse> orders = adminOrderService.getAllOrders(filterRequest, pageable);

		// ✅ ENHANCED SUCCESS RESPONSE MESSAGE
		String message = String.format("🔍 Tìm kiếm linh hoạt thành công!  Tìm thấy %d/%d đơn hàng (trang %d/%d)",
				orders.getContent().size(), orders.getTotalElements(), page + 1, orders.getTotalPages());

		return ResponseEntity.ok(ApiResponse.success(message, orders));
	}

	/**
	 * GET /api/v1/admin/orders/{orderId} Get order detail by ID
	 */
	@GetMapping("/{orderId}")
	@Operation(summary = "Lấy chi tiết đơn hàng (Admin)", description = "Lấy thông tin chi tiết một đơn hàng.  Chỉ Admin mới truy cập được.")
	public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> getOrderDetail(
			@Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {

		log.info("Admin getting order detail:  {}", orderId);

		AdminOrderDetailResponse orderDetail = adminOrderService.getOrderDetail(orderId);

		return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", orderDetail));
	}

	/**
	 * PUT /api/v1/admin/orders/{orderId}/status Update order status
	 */
	@PutMapping("/{orderId}/status")
	@Operation(summary = "Cập nhật trạng thái đơn hàng (Admin)", description = "Cập nhật trạng thái đơn hàng theo quy tắc nghiệp vụ.  Chỉ Admin mới thực hiện được.")
	public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> updateOrderStatus(
			@Parameter(description = "Order ID", required = true) @PathVariable Long orderId,

			@Parameter(description = "New order status", required = true) @RequestParam OrderStatus newStatus,

			@Parameter(description = "Admin note/reason for status change") @RequestParam(required = false) String adminNote) {

		log.info("Admin updating order {} to status: {}", orderId, newStatus);

		AdminOrderDetailResponse updatedOrder = adminOrderService.updateOrderStatus(orderId, newStatus, adminNote);

		return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", updatedOrder));
	}

	/**
	 * GET /api/v1/admin/orders/statistics Get order statistics for dashboard
	 */
	@GetMapping("/statistics")
	@Operation(summary = "Lấy thống kê đơn hàng (Admin)", description = "Lấy các số liệu thống kê đơn hàng cho dashboard admin")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatistics() {

		log.info("Admin getting order statistics");

		Map<String, Object> statistics = adminOrderService.getOrderStatistics();

		return ResponseEntity.ok(ApiResponse.success("Lấy thống kê đơn hàng thành công", statistics));
	}

	/**
	 * GET /api/v1/admin/orders/recent Get recent orders
	 */
	@GetMapping("/recent")
	@Operation(summary = "Lấy đơn hàng gần đây (Admin)", description = "Lấy danh sách đơn hàng mới nhất cho dashboard admin")
	public ResponseEntity<ApiResponse<List<AdminOrderListResponse>>> getRecentOrders(
			@Parameter(description = "Limit number of orders") @RequestParam(defaultValue = "10") int limit) {

		log.info("Admin getting {} recent orders", limit);

		List<AdminOrderListResponse> recentOrders = adminOrderService.getRecentOrders(limit);

		return ResponseEntity.ok(ApiResponse.success("Lấy đơn hàng gần đây thành công", recentOrders));
	}

	/**
	 * GET /api/v1/admin/orders/{orderId}/available-transitions Get available status
	 * transitions for an order
	 */
	@GetMapping("/{orderId}/available-transitions")
	@Operation(summary = "Lấy các trạng thái có thể chuyển (Admin)", description = "Lấy danh sách các trạng thái mà đơn hàng có thể chuyển sang")
	public ResponseEntity<ApiResponse<List<OrderStatus>>> getAvailableTransitions(
			@Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {

		log.info("Admin getting available transitions for order: {}", orderId);

		List<OrderStatus> transitions = adminOrderService.getAvailableStatusTransitions(orderId);

		return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái khả dụng thành công", transitions));
	}

	/**
	 * GET /api/v1/admin/orders/customer/{customerId} Get orders by customer
	 */
	@GetMapping("/customer/{customerId}")
	@Operation(summary = "Lấy đơn hàng theo khách hàng (Admin)", description = "Lấy tất cả đơn hàng của một khách hàng cụ thể")
	public ResponseEntity<ApiResponse<List<AdminOrderListResponse>>> getOrdersByCustomer(
			@Parameter(description = "Customer ID", required = true) @PathVariable Long customerId) {

		log.info("Admin getting orders for customer:  {}", customerId);

		List<AdminOrderListResponse> customerOrders = adminOrderService.getOrdersByCustomer(customerId);

		return ResponseEntity.ok(ApiResponse.success("Lấy đơn hàng của khách hàng thành công", customerOrders));
	}
}