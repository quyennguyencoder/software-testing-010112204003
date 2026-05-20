package com.utephonehub.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utephonehub.backend.dto.request.order.CreateOrderRequest;
import com.utephonehub.backend.dto.response.order.CreateOrderResponse;
import com.utephonehub.backend.dto.response.order.OrderResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.servlet.http.HttpServletRequest;

public interface IOrderService {
    
    /**
     * Lấy thông tin đơn hàng theo ID
     * @param orderId ID của đơn hàng
     * @param userId ID của user (để check quyền sở hữu)
     * @return Thông tin đơn hàng
     */
    OrderResponse getOrderById(Long orderId, Long userId);
    
    /**
     * Tạo đơn hàng mới
     * @param request Thông tin đơn hàng
     * @param userId ID của user tạo đơn
     * @param servletRequest Request context (để lấy IP cho VNPay)
     * @return Thông tin đơn hàng đã tạo
     */
    CreateOrderResponse createOrder(CreateOrderRequest request, Long userId);

	List<OrderResponse> getMyOrdersByStatus(Long userId, OrderStatus status);

	List<OrderResponse> getMyOrders(Long userId);

	long getMyOrdersCount(Long userId);

	Page<OrderResponse> getMyOrdersWithPagination(Long userId, Pageable pageable);

	OrderResponse getMyOrderDetail(Long orderId, Long userId);

	boolean canCancelOrder(Long orderId, Long userId);

	void cancelMyOrder(Long orderId, Long userId);
	
  CreateOrderResponse createOrder(CreateOrderRequest request, Long userId, HttpServletRequest servletRequest);
}
