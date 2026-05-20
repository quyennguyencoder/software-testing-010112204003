
package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.order.AdminOrderFilterRequest;
import com.utephonehub.backend.dto.response.order.AdminOrderDetailResponse;
import com.utephonehub.backend.dto.response.order.AdminOrderListResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.service.IAdminOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements IAdminOrderService {

	private final OrderRepository orderRepository;

	@Override
	@Transactional(readOnly = true)
	public Page<AdminOrderListResponse> getAllOrders(AdminOrderFilterRequest filterRequest, Pageable pageable) {
		log.info("Admin getting all orders with flexible filters:  {}", filterRequest);

		// ✅ SMART PARAMETER PROCESSING

		// Normalize search keyword
		String search = filterRequest.getSearch();
		if (search != null && search.trim().isEmpty()) {
			search = null; // Convert empty string to null
		}

		// Normalize customer email
		String customerEmail = filterRequest.getCustomerEmail();
		if (customerEmail != null && customerEmail.trim().isEmpty()) {
			customerEmail = null; // Convert empty string to null
		}

		// ✅ SMART DATE HANDLING
		LocalDateTime fromDateTime = null;
		LocalDateTime toDateTime = null;

		if (filterRequest.getFromDate() != null) {
			fromDateTime = filterRequest.getFromDate().atStartOfDay();
			log.debug("Filtering from date: {}", fromDateTime);
		}

		if (filterRequest.getToDate() != null) {
			toDateTime = filterRequest.getToDate().atTime(LocalTime.MAX);
			log.debug("Filtering to date: {}", toDateTime);
		}

		// ✅ SMART AMOUNT HANDLING
		BigDecimal minAmount = filterRequest.getMinAmount();
		BigDecimal maxAmount = filterRequest.getMaxAmount();

		// Convert zero amounts to null (don't filter)
		if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) <= 0) {
			minAmount = null;
		}
		if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
			maxAmount = null;
		}

		// ✅ EXECUTE FLEXIBLE QUERY
		Page<Order> orderPage = orderRepository.findOrdersWithFlexibleFilters(search, // null if empty
				filterRequest.getStatus(), // null if not specified
				filterRequest.getPaymentMethod(), // null if not specified
				filterRequest.getCustomerId(), // null if not specified
				customerEmail, // null if empty
				fromDateTime, // null if not specified
				toDateTime, // null if not specified
				minAmount, // null if zero or negative
				maxAmount, // null if zero or negative
				pageable);

		log.info("Found {} orders matching flexible filters (total: {})", orderPage.getContent().size(),
				orderPage.getTotalElements());

		// Convert to response DTOs
		return orderPage.map(AdminOrderListResponse::fromEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public AdminOrderDetailResponse getOrderDetail(Long orderId) {
		log.info("Admin getting order detail for ID: {}", orderId);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found:  {}", orderId);
			return new ResourceNotFoundException("Order not found with ID: " + orderId);
		});

		log.info("Found order: {}", order.getOrderCode());
		return AdminOrderDetailResponse.fromEntity(order);
	}

	@Override
	@Transactional
	public AdminOrderDetailResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String adminNote) {
		log.info("Admin updating order {} to status: {}", orderId, newStatus);

		Order order = orderRepository.findById(orderId).orElseThrow(() -> {
			log.error("Order not found: {}", orderId);
			return new ResourceNotFoundException("Order not found with ID: " + orderId);
		});

		// Validate status transition
		if (!isValidStatusTransition(order.getStatus(), newStatus)) {
			log.warn("Invalid status transition from {} to {} for order {}", order.getStatus(), newStatus, orderId);
			throw new BadRequestException(
					String.format("Cannot change order status from %s to %s", order.getStatus(), newStatus));
		}

		// Update order status
		OrderStatus oldStatus = order.getStatus();
		order.setStatus(newStatus);
		order.setUpdatedAt(LocalDateTime.now());

		// Save order
		Order updatedOrder = orderRepository.save(order);

		log.info("Successfully updated order {} from {} to {}", orderId, oldStatus, newStatus);

		// TODO: Save status history with admin note (can be enhanced later)

		return AdminOrderDetailResponse.fromEntity(updatedOrder);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getOrderStatistics() {
		log.info("Getting order statistics for admin dashboard");

		try {
			// Get order counts by status
			List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
			Map<String, Long> statusMap = new HashMap<>();

			for (Object[] row : statusCounts) {
				OrderStatus status = (OrderStatus) row[0];
				Long count = (Long) row[1];
				statusMap.put(status.name(), count);
			}

			// Calculate totals
			long totalOrders = orderRepository.count();
			long pendingOrders = statusMap.getOrDefault("PENDING", 0L);
			long deliveredOrders = statusMap.getOrDefault("DELIVERED", 0L);
			long cancelledOrders = statusMap.getOrDefault("CANCELLED", 0L);

			// Calculate delivery rate
			double deliveryRate = totalOrders > 0 ? (double) deliveredOrders / totalOrders * 100 : 0;

			// Get revenue for current month
			LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
			LocalDateTime now = LocalDateTime.now();
			BigDecimal monthlyRevenue = orderRepository.getTotalRevenueInDateRange(startOfMonth, now);

			Map<String, Object> statistics = new HashMap<>();
			statistics.put("totalOrders", totalOrders);
			statistics.put("pendingOrders", pendingOrders);
			statistics.put("confirmedOrders", statusMap.getOrDefault("CONFIRMED", 0L));
			statistics.put("shippingOrders", statusMap.getOrDefault("SHIPPING", 0L));
			statistics.put("deliveredOrders", deliveredOrders);
			statistics.put("cancelledOrders", cancelledOrders);
			statistics.put("deliveryRate", Math.round(deliveryRate * 100.0) / 100.0);
			statistics.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
			statistics.put("statusBreakdown", statusMap);

			log.info("Generated order statistics: {} total orders", totalOrders);
			return statistics;

		} catch (Exception e) {
			log.error("Error generating order statistics:  {}", e.getMessage());
			return Map.of("error", "Unable to fetch statistics");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminOrderListResponse> getRecentOrders(int limit) {
		log.info("Getting {} recent orders for admin", limit);

		Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
		List<Order> recentOrders = orderRepository.findRecentOrdersLimit(pageable);

		return recentOrders.stream().map(AdminOrderListResponse::fromEntity).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminOrderListResponse> getOrdersByCustomer(Long customerId) {
		log.info("Getting orders for customer:  {}", customerId);

		List<Order> customerOrders = orderRepository.findOrdersByUserWithDetails(customerId);

		return customerOrders.stream().map(AdminOrderListResponse::fromEntity).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderStatus> getAvailableStatusTransitions(Long orderId) {
		log.info("Getting available status transitions for order: {}", orderId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

		return getValidNextStatuses(order.getStatus());
	}

	@Override
	@Transactional
	public void bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus, String adminNote) {
		log.info("Bulk updating {} orders to status: {}", orderIds.size(), newStatus);

		List<Order> orders = orderRepository.findAllById(orderIds);

		for (Order order : orders) {
			if (isValidStatusTransition(order.getStatus(), newStatus)) {
				order.setStatus(newStatus);
				order.setUpdatedAt(LocalDateTime.now());
			} else {
				log.warn("Skipping invalid transition for order {}:  {} -> {}", order.getId(), order.getStatus(),
						newStatus);
			}
		}

		orderRepository.saveAll(orders);
		log.info("Completed bulk update for {} orders", orders.size());
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> getOrderSummaryStats() {
		log.info("Getting order summary statistics");

		try {
			long totalOrders = orderRepository.count();

			// Get today's orders
			LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
			LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

			// Calculate revenue
			BigDecimal totalRevenue = orderRepository.getTotalRevenueInDateRange(LocalDateTime.now().minusYears(10),
					LocalDateTime.now());

			Map<String, Object> summary = new HashMap<>();
			summary.put("totalOrders", totalOrders);
			summary.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
			summary.put("averageOrderValue",
					totalOrders > 0 && totalRevenue != null
							? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
							: BigDecimal.ZERO);

			return summary;

		} catch (Exception e) {
			log.error("Error getting order summary stats: {}", e.getMessage());
			return Map.of("error", "Unable to fetch summary statistics");
		}
	}

	// ========================================
	// HELPER METHODS
	// ========================================

	private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
		List<OrderStatus> validTransitions = getValidNextStatuses(currentStatus);
		return validTransitions.contains(newStatus);
	}

	private List<OrderStatus> getValidNextStatuses(OrderStatus currentStatus) {
		return switch (currentStatus) {
		case PENDING -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
		case CONFIRMED -> List.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED);
		case SHIPPING -> List.of(OrderStatus.DELIVERED);
		case DELIVERED, CANCELLED -> List.of(); // No further transitions
		};
	}
}