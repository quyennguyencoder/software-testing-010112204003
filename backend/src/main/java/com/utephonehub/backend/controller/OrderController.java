package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.order.CreateOrderRequest;
import com.utephonehub.backend.dto.response.order.CreateOrderResponse;
import com.utephonehub.backend.dto.response.order.OrderResponse;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.service.IOrderService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "API quản lý đơn hàng")
public class OrderController {

	private final IOrderService orderService;
	private final SecurityUtils securityUtils;

	/**
	 * GET /api/v1/orders/{orderId} Lấy thông tin đơn hàng theo ID
	 */
	@GetMapping("/{orderId}")
	@Operation(summary = "Lấy thông tin đơn hàng", description = "User chỉ có thể xem đơn hàng của chính mình")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId,
			HttpServletRequest request) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(request);

		// Gọi service
		OrderResponse response = orderService.getOrderById(orderId, userId);

		// Trả về response
		return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đơn hàng thành công", response));
	}

	/**
	 * POST /api/v1/orders Tạo đơn hàng mới
	 */
	@PostMapping
	@Operation(summary = "Tạo đơn hàng mới", description = "Tạo đơn hàng với danh sách sản phẩm. Hỗ trợ COD, VNPay, Bank Transfer.")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request,
			HttpServletRequest httpRequest) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(httpRequest);

		// Gọi service tạo đơn hàng, truyền kèm HttpServletRequest để lấy IP client (phục vụ VNPay)
		CreateOrderResponse response = orderService.createOrder(request, userId, httpRequest);

		// Trả về response với status 201 Created
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Tạo đơn hàng thành công", response));
	}

	/**
	 * GET /api/v1/orders/my-orders Xem lịch sử đơn hàng của tôi
	 */
	@GetMapping("/my-orders")
	@Operation(summary = "Xem lịch sử đơn hàng của tôi", description = "Lấy danh sách tất cả đơn hàng của khách hàng hiện tại, sắp xếp theo ngày tạo mới nhất")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(HttpServletRequest request) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(request);

		// Gọi service
		List<OrderResponse> orders = orderService.getMyOrders(userId);

		// Trả về response
		return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", orders));
	}

	/**
	 * GET /api/v1/orders/my-orders/paginated Xem lịch sử đơn hàng với phân trang
	 */
	@GetMapping("/my-orders/paginated")
	@Operation(summary = "Xem lịch sử đơn hàng (có phân trang)", description = "Lấy danh sách đơn hàng của khách hàng với phân trang và sắp xếp")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrdersPaginated(
			@Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Số lượng đơn hàng mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Hướng sắp xếp (asc/desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
			HttpServletRequest request) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(request);

		// Tạo Pageable với sorting
		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		// Gọi service
		Page<OrderResponse> orderPage = orderService.getMyOrdersWithPagination(userId, pageable);

		// Trả về response
		return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", orderPage));
	}

	/**
	 * GET /api/v1/orders/my-orders/by-status Lọc đơn hàng theo trạng thái
	 */
	@GetMapping("/my-orders/by-status")
	@Operation(summary = "Lọc đơn hàng theo trạng thái", description = "Lấy danh sách đơn hàng của khách hàng theo trạng thái cụ thể")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrdersByStatus(
			@Parameter(description = "Trạng thái đơn hàng", required = true, example = "DELIVERED") @RequestParam OrderStatus status,
			HttpServletRequest request) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(request);

		// Gọi service
		List<OrderResponse> orders = orderService.getMyOrdersByStatus(userId, status);

		// Trả về response
		return ResponseEntity.ok(ApiResponse.success("Lọc đơn hàng theo trạng thái thành công", orders));
	}

	/**
	 * GET /api/v1/orders/my-orders/count Đếm số đơn hàng của tôi
	 */
	@GetMapping("/my-orders/count")
	@Operation(summary = "Đếm số đơn hàng của tôi", description = "Lấy tổng số đơn hàng của khách hàng hiện tại")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Long>> getMyOrdersCount(HttpServletRequest request) {

		// Lấy userId từ JWT token
		Long userId = securityUtils.getCurrentUserId(request);

		// Gọi service
		long count = orderService.getMyOrdersCount(userId);

		// Trả về response
		return ResponseEntity.ok(ApiResponse.success("Đếm số đơn hàng thành công", count));
	}

	// ========================================
	// ✅ CHỨC NĂNG HỦY ĐƠN HÀNG
	// ========================================

	@PostMapping("/{orderId}/cancel")
	@Operation(summary = "Hủy đơn hàng", description = "Khách hàng tự hủy đơn hàng của mình.  Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ xác nhận'.")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<String>> cancelMyOrder(
			@Parameter(description = "ID đơn hàng", required = true, example = "1") @PathVariable Long orderId,
			HttpServletRequest request) {

		Long userId = securityUtils.getCurrentUserId(request);
		orderService.cancelMyOrder(orderId, userId);

		return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công",
				"Đơn hàng đã được hủy.  Nếu đã thanh toán, chúng tôi sẽ hoàn tiền trong 3-5 ngày làm việc."));
	}

	@GetMapping("/{orderId}/can-cancel")
	@Operation(summary = "Kiểm tra có thể hủy đơn hàng", description = "Kiểm tra xem đơn hàng có thể bị hủy bởi khách hàng hay không")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Boolean>> canCancelOrder(
			@Parameter(description = "ID đơn hàng", required = true, example = "1") @PathVariable Long orderId,
			HttpServletRequest request) {

		Long userId = securityUtils.getCurrentUserId(request);
		boolean canCancel = orderService.canCancelOrder(orderId, userId);

		String message = canCancel ? "Đơn hàng có thể bị hủy"
				: "Đơn hàng không thể hủy (không phải trạng thái 'Chờ xác nhận' hoặc không thuộc về bạn)";

		return ResponseEntity.ok(ApiResponse.success(message, canCancel));
	}

	@GetMapping("/{orderId}/status-info")
	@Operation(
	    summary = "Lấy thông tin trạng thái đơn hàng",
	    description = "Lấy thông tin chi tiết về trạng thái hiện tại và các hành động khả thi"
	)
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatusInfo(
	        @Parameter(description = "ID đơn hàng", required = true, example = "1")
	        @PathVariable Long orderId,
	        HttpServletRequest request) {
	    
	    Long userId = securityUtils.getCurrentUserId(request);
	    OrderResponse order = orderService.getOrderById(orderId, userId);
	    boolean canCancel = orderService.canCancelOrder(orderId, userId);
	    
	    Map<String, Object> statusInfo = Map.of(
	        "orderId", order.getId(),
	        "orderCode", order.getOrderCode(),
	        "currentStatus", order.getStatus(),
	        "statusDisplay", getStatusDisplayName(order.getStatus()),
	        "canCancel", canCancel,
	        "statusDescription", getStatusDescription(order.getStatus()),
	        "availableActions", canCancel ? 
	            new String[]{"cancel", "view_detail"} :  new String[]{"view_detail"}
	    );
	    
	    return ResponseEntity.ok(
	        ApiResponse.success("Lấy thông tin trạng thái thành công", statusInfo)
	    );
	}
	
	private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELLED -> "Đã hủy";
        };
    }
    
    private String getStatusDescription(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đơn hàng đang chờ cửa hàng xác nhận. Thời gian xử lý: 1-2 giờ làm việc.";
            case CONFIRMED -> "Đơn hàng đã được xác nhận và đang chuẩn bị hàng hóa.";
            case SHIPPING -> "Đơn hàng đang được giao đến địa chỉ của bạn.";
            case DELIVERED -> "Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng!";
            case CANCELLED -> "Đơn hàng đã bị hủy.";
        };
    }
    

}
