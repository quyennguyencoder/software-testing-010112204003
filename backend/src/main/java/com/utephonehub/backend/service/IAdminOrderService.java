
package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.order.AdminOrderFilterRequest;
import com.utephonehub.backend.dto.response.order.AdminOrderDetailResponse;
import com.utephonehub.backend.dto.response.order.AdminOrderListResponse;
import com.utephonehub.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IAdminOrderService {

	/**
	 * Get all orders with pagination and filtering (Admin only)
	 */
	Page<AdminOrderListResponse> getAllOrders(AdminOrderFilterRequest filterRequest, Pageable pageable);

	/**
	 * Get order detail by ID (Admin only)
	 */
	AdminOrderDetailResponse getOrderDetail(Long orderId);

	/**
	 * Update order status (Admin only)
	 */
	AdminOrderDetailResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String adminNote);

	/**
	 * Get order statistics for admin dashboard
	 */
	Map<String, Object> getOrderStatistics();

	/**
	 * Get recent orders for admin dashboard
	 */
	List<AdminOrderListResponse> getRecentOrders(int limit);

	/**
	 * Get orders by customer ID (Admin view)
	 */
	List<AdminOrderListResponse> getOrdersByCustomer(Long customerId);

	/**
	 * Get available status transitions for an order
	 */
	List<OrderStatus> getAvailableStatusTransitions(Long orderId);

	/**
	 * Bulk update order status (Admin only)
	 */
	void bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus, String adminNote);

	/**
	 * Get order summary statistics
	 */
	Map<String, Object> getOrderSummaryStats();
}