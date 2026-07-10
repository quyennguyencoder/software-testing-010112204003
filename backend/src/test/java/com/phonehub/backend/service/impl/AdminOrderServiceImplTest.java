package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.order.AdminOrderFilterRequest;
import com.phonehub.backend.dto.response.order.AdminOrderDetailResponse;
import com.phonehub.backend.dto.response.order.AdminOrderListResponse;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    @Test
    @DisplayName("Nên lấy danh sách đơn hàng thành công")
    void getAllOrders_Success() {
        AdminOrderFilterRequest filterRequest = new AdminOrderFilterRequest();
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderCode("ORD123");
        mockOrder.setStatus(OrderStatus.PENDING);
        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder));

        when(orderRepository.findOrdersWithFlexibleFilters(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(orderPage);

        Page<AdminOrderListResponse> result = adminOrderService.getAllOrders(filterRequest, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD123", result.getContent().get(0).getOrderCode());
    }

    @Test
    @DisplayName("Nên chuẩn hóa bộ lọc rỗng và gọi repository với giá trị null")
    void getAllOrders_NormalizeEmptyFilterValues() {
        AdminOrderFilterRequest filterRequest = new AdminOrderFilterRequest();
        filterRequest.setSearch("   ");
        filterRequest.setCustomerEmail("   ");
        filterRequest.setFromDate(LocalDate.of(2026, 1, 1));
        filterRequest.setToDate(LocalDate.of(2026, 1, 10));
        filterRequest.setMinAmount(BigDecimal.ZERO);
        filterRequest.setMaxAmount(BigDecimal.valueOf(-100));

        Order mockOrder = new Order();
        mockOrder.setId(2L);
        mockOrder.setOrderCode("ORD456");
        mockOrder.setStatus(OrderStatus.CONFIRMED);
        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder));

        when(orderRepository.findOrdersWithFlexibleFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(orderPage);

        Page<AdminOrderListResponse> result = adminOrderService.getAllOrders(filterRequest, PageRequest.of(0, 5));

        verify(orderRepository).findOrdersWithFlexibleFilters(isNull(), any(), any(), any(), isNull(), any(), any(), isNull(), isNull(), any());
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD456", result.getContent().get(0).getOrderCode());
    }

    @Test
    @DisplayName("Nên sử dụng các giá trị bộ lọc không rỗng")
    void getAllOrders_UsesPositiveFilterValues() {
        AdminOrderFilterRequest filterRequest = new AdminOrderFilterRequest();
        filterRequest.setSearch("ORD");
        filterRequest.setCustomerEmail("email@example.com");
        filterRequest.setMinAmount(BigDecimal.valueOf(1000));
        filterRequest.setMaxAmount(BigDecimal.valueOf(10000));

        Order mockOrder = new Order();
        mockOrder.setId(3L);
        mockOrder.setOrderCode("ORD789");
        mockOrder.setStatus(OrderStatus.SHIPPING);
        Page<Order> orderPage = new PageImpl<>(List.of(mockOrder));

        when(orderRepository.findOrdersWithFlexibleFilters(eq("ORD"), any(), any(), any(), eq("email@example.com"), any(), any(), eq(BigDecimal.valueOf(1000)), eq(BigDecimal.valueOf(10000)), any()))
                .thenReturn(orderPage);

        Page<AdminOrderListResponse> result = adminOrderService.getAllOrders(filterRequest, PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD789", result.getContent().get(0).getOrderCode());
    }

    @Test
    @DisplayName("Nên ném ResourceNotFoundException khi chi tiết đơn hàng không tồn tại")
    void getOrderDetail_ThrowsNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminOrderService.getOrderDetail(1L));
    }

    @Test
    @DisplayName("Nên ném ResourceNotFoundException khi cập nhật trạng thái đơn hàng không tồn tại")
    void updateOrderStatus_ThrowsNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminOrderService.updateOrderStatus(1L, OrderStatus.CONFIRMED, "Note"));
    }

    @Test
    @DisplayName("Nên trả về monthlyRevenue bằng 0 khi repository trả về null")
    void getOrderStatistics_NullMonthlyRevenue_ReturnsZero() {
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.countOrdersByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{OrderStatus.PENDING, 10L}
        ));
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(null);

        Map<String, Object> stats = adminOrderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalOrders"));
        assertEquals(10L, stats.get("pendingOrders"));
        assertEquals(0L, stats.get("deliveredOrders"));
        assertEquals(BigDecimal.ZERO, stats.get("monthlyRevenue"));
    }

    @Test
    @DisplayName("Nên trả về map lỗi khi thống kê đơn hàng gặp exception")
    void getOrderStatistics_WhenRepositoryThrows_ReturnsErrorMap() {
        when(orderRepository.countOrdersByStatus()).thenThrow(new RuntimeException("DB error"));

        Map<String, Object> stats = adminOrderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals("Unable to fetch statistics", stats.get("error"));
    }

    @Test
    @DisplayName("Nên lấy đơn hàng gần đây thành công")
    void getRecentOrders_Success() {
        Order mockOrder = new Order();
        mockOrder.setId(4L);
        mockOrder.setOrderCode("ORD_RECENT");
        mockOrder.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findRecentOrdersLimit(any())).thenReturn(List.of(mockOrder));

        List<AdminOrderListResponse> result = adminOrderService.getRecentOrders(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORD_RECENT", result.get(0).getOrderCode());
    }

    @Test
    @DisplayName("Nên lấy đơn hàng của khách hàng thành công")
    void getOrdersByCustomer_Success() {
        User user = new User();
        user.setId(5L);
        user.setFullName("Nguyen Van A");
        user.setPhoneNumber("0900000000");

        Order order = new Order();
        order.setId(10L);
        order.setOrderCode("ORDCUST");
        order.setStatus(OrderStatus.SHIPPED);
        order.setUser(user);
        order.setEmail("customer@example.com");

        when(orderRepository.findOrdersByUserWithDetails(5L)).thenReturn(List.of(order));

        List<AdminOrderListResponse> result = adminOrderService.getOrdersByCustomer(5L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORDCUST", result.get(0).getOrderCode());
        assertEquals("Nguyen Van A", result.get(0).getCustomerName());
    }

    @Test
    @DisplayName("Nên trả về trạng thái tiếp theo cho đơn hàng SHIPPING")
    void getAvailableStatusTransitions_Shipping() {
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setStatus(OrderStatus.SHIPPING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        List<OrderStatus> statuses = adminOrderService.getAvailableStatusTransitions(1L);

        assertNotNull(statuses);
        assertEquals(List.of(OrderStatus.DELIVERED), statuses);
    }

    @Test
    @DisplayName("Nên trả về trạng thái tiếp theo cho đơn hàng PENDING")
    void getAvailableStatusTransitions_Pending() {
        Order mockOrder = new Order();
        mockOrder.setId(2L);
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(2L)).thenReturn(Optional.of(mockOrder));

        List<OrderStatus> statuses = adminOrderService.getAvailableStatusTransitions(2L);

        assertNotNull(statuses);
        assertEquals(List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED), statuses);
    }

    @Test
    @DisplayName("Nên trả về trạng thái tiếp theo cho đơn hàng CONFIRMED")
    void getAvailableStatusTransitions_Confirmed() {
        Order mockOrder = new Order();
        mockOrder.setId(3L);
        mockOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(mockOrder));

        List<OrderStatus> statuses = adminOrderService.getAvailableStatusTransitions(3L);

        assertNotNull(statuses);
        assertEquals(List.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED), statuses);
    }

    @Test
    @DisplayName("Nên trả về trạng thái tiếp theo cho đơn hàng SHIPPED")
    void getAvailableStatusTransitions_Shipped() {
        Order mockOrder = new Order();
        mockOrder.setId(4L);
        mockOrder.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(4L)).thenReturn(Optional.of(mockOrder));

        List<OrderStatus> statuses = adminOrderService.getAvailableStatusTransitions(4L);

        assertNotNull(statuses);
        assertEquals(List.of(OrderStatus.DELIVERED), statuses);
    }

    @Test
    @DisplayName("Nên ném lỗi khi không tìm thấy đơn hàng khi lấy trạng thái tiếp theo")
    void getAvailableStatusTransitions_ThrowsNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminOrderService.getAvailableStatusTransitions(1L));
    }

    @Test
    @DisplayName("Nên cập nhật hàng loạt và bỏ qua đơn hàng có chuyển trạng thái không hợp lệ")
    void bulkUpdateOrderStatus_SkipsInvalidTransitions() {
        Order validOrder = new Order();
        validOrder.setId(1L);
        validOrder.setStatus(OrderStatus.PENDING);

        Order invalidOrder = new Order();
        invalidOrder.setId(2L);
        invalidOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(validOrder, invalidOrder));

        adminOrderService.bulkUpdateOrderStatus(List.of(1L, 2L), OrderStatus.CONFIRMED, "Bulk note");

        assertEquals(OrderStatus.CONFIRMED, validOrder.getStatus());
        assertEquals(OrderStatus.DELIVERED, invalidOrder.getStatus());
        verify(orderRepository, times(1)).saveAll(List.of(validOrder, invalidOrder));
    }

    @Test
    @DisplayName("Nên trả về summary stats với averageOrderValue = 0 khi không có đơn hàng")
    void getOrderSummaryStats_NoOrders_ReturnsZeroAverage() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(null);

        Map<String, Object> summary = adminOrderService.getOrderSummaryStats();

        assertNotNull(summary);
        assertEquals(0L, summary.get("totalOrders"));
        assertEquals(BigDecimal.ZERO, summary.get("totalRevenue"));
        assertEquals(BigDecimal.ZERO, summary.get("averageOrderValue"));
    }

    @Test
    @DisplayName("Nên trả về summary stats với averageOrderValue được tính đúng")
    void getOrderSummaryStats_WithOrders_ReturnsCalculatedAverage() {
        when(orderRepository.count()).thenReturn(4L);
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(new BigDecimal("2000"));

        Map<String, Object> summary = adminOrderService.getOrderSummaryStats();

        assertNotNull(summary);
        assertEquals(4L, summary.get("totalOrders"));
        assertEquals(new BigDecimal("2000"), summary.get("totalRevenue"));
        assertEquals(new BigDecimal("500.00"), summary.get("averageOrderValue"));
    }

    @Test
    @DisplayName("Nên trả về summary stats khi có đơn hàng nhưng tổng doanh thu null")
    void getOrderSummaryStats_WithOrdersAndNullRevenue_ReturnsZeroAverage() {
        when(orderRepository.count()).thenReturn(3L);
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(null);

        Map<String, Object> summary = adminOrderService.getOrderSummaryStats();

        assertNotNull(summary);
        assertEquals(3L, summary.get("totalOrders"));
        assertEquals(BigDecimal.ZERO, summary.get("totalRevenue"));
        assertEquals(BigDecimal.ZERO, summary.get("averageOrderValue"));
    }

    @Test
    @DisplayName("Nên trả về map lỗi khi summary statistics gặp exception")
    void getOrderSummaryStats_WhenRepositoryThrows_ReturnsErrorMap() {
        when(orderRepository.count()).thenThrow(new RuntimeException("DB error"));

        Map<String, Object> summary = adminOrderService.getOrderSummaryStats();

        assertNotNull(summary);
        assertEquals("Unable to fetch summary statistics", summary.get("error"));
    }

    @Test
    @DisplayName("Nên lấy chi tiết đơn hàng thành công")
    void getOrderDetail_Success() {
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderCode("ORD123");
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        AdminOrderDetailResponse response = adminOrderService.getOrderDetail(1L);

        assertNotNull(response);
        assertEquals("ORD123", response.getOrderCode());
    }

    @Test
    @DisplayName("Nên cập nhật trạng thái đơn hàng thành công")
    void updateOrderStatus_Success() {
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        AdminOrderDetailResponse response = adminOrderService.updateOrderStatus(1L, OrderStatus.CONFIRMED, "Note");

        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED, mockOrder.getStatus());
        verify(orderRepository, times(1)).save(mockOrder);
    }

    @Test
    @DisplayName("Nên ném lỗi BadRequestException khi chuyển trạng thái không hợp lệ")
    void updateOrderStatus_ThrowsException_WhenInvalidTransition() {
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setStatus(OrderStatus.DELIVERED); // Không thể chuyển từ DELIVERED

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(BadRequestException.class, () -> 
                adminOrderService.updateOrderStatus(1L, OrderStatus.PENDING, "Note"));
    }

    @Test
    @DisplayName("Nên lấy thống kê đơn hàng thành công")
    void getOrderStatistics_Success() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countOrdersByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{OrderStatus.DELIVERED, 80L},
                new Object[]{OrderStatus.PENDING, 20L}
        ));
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(new BigDecimal("5000000"));

        Map<String, Object> stats = adminOrderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals(100L, stats.get("totalOrders"));
        assertEquals(80L, stats.get("deliveredOrders"));
        assertEquals(20L, stats.get("pendingOrders"));
        assertEquals(new BigDecimal("5000000"), stats.get("monthlyRevenue"));
    }

    @Test
    @DisplayName("Nên lấy thống kê đơn hàng với tất cả trạng thái và tính delivery rate")
    void getOrderStatistics_AllStatuses_ReturnsFullBreakdown() {
        when(orderRepository.count()).thenReturn(300L);
        when(orderRepository.countOrdersByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{OrderStatus.PENDING, 20L},
                new Object[]{OrderStatus.CONFIRMED, 50L},
                new Object[]{OrderStatus.SHIPPING, 30L},
                new Object[]{OrderStatus.DELIVERED, 150L},
                new Object[]{OrderStatus.CANCELLED, 50L}
        ));
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(new BigDecimal("100000"));

        Map<String, Object> stats = adminOrderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals(300L, stats.get("totalOrders"));
        assertEquals(150L, stats.get("deliveredOrders"));
        assertEquals(20L, stats.get("pendingOrders"));
        assertEquals(50L, stats.get("confirmedOrders"));
        assertEquals(30L, stats.get("shippingOrders"));
        assertEquals(50L, stats.get("cancelledOrders"));
        assertEquals(50.0, stats.get("deliveryRate"));
        assertEquals(new BigDecimal("100000"), stats.get("monthlyRevenue"));
    }

    @Test
    @DisplayName("Nên trả về thống kê đơn hàng với tổng đơn 0 và deliveryRate 0")
    void getOrderStatistics_ZeroOrders_ReturnsZeroRate() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.countOrdersByStatus()).thenReturn(List.of());
        when(orderRepository.getTotalRevenueInDateRange(any(), any())).thenReturn(null);

        Map<String, Object> stats = adminOrderService.getOrderStatistics();

        assertNotNull(stats);
        assertEquals(0L, stats.get("totalOrders"));
        assertEquals(0L, stats.get("pendingOrders"));
        assertEquals(0.0, stats.get("deliveryRate"));
        assertEquals(BigDecimal.ZERO, stats.get("monthlyRevenue"));
    }
}
