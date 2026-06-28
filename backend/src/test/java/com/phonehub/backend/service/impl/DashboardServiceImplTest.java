package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.response.dashboard.*;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.enums.DashboardPeriod;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.repository.OrderItemRepository;
import com.phonehub.backend.repository.OrderRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("Nên lấy thống kê tổng quan thành công")
    void getOverview_Success() {
        when(orderRepository.calculateTotalRevenueByStatus(OrderStatus.DELIVERED)).thenReturn(new BigDecimal("1000000"));
        when(orderRepository.count()).thenReturn(100L);
        when(productRepository.count()).thenReturn(50L);
        when(userRepository.count()).thenReturn(20L);

        DashboardOverviewResponse response = dashboardService.getOverview();

        assertNotNull(response);
        assertEquals(new BigDecimal("1000000"), response.getTotalRevenue());
        assertEquals(100L, response.getTotalOrders());
        assertEquals(50L, response.getTotalProducts());
        assertEquals(20L, response.getTotalUsers());
    }

    @Test
    @DisplayName("Nên lấy biểu đồ trạng thái đơn hàng thành công")
    void getOrderStatusChart_Success() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(10L);
        when(orderRepository.countByStatus(OrderStatus.CONFIRMED)).thenReturn(20L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPING)).thenReturn(30L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(30L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(10L);

        OrderStatusChartResponse response = dashboardService.getOrderStatusChart();

        assertNotNull(response);
        assertEquals(100L, response.getTotalOrders());
        assertEquals(6, response.getLabels().size());
        assertEquals(6, response.getValues().size());
        assertTrue(response.getLabels().contains("Chờ xác nhận"));
        assertEquals(10L, response.getValues().get(0)); // 10%
    }

    @Test
    @DisplayName("Nên lấy top sản phẩm bán chạy thành công")
    void getTopProducts_Success() {
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("iPhone 15");

        Object[] row = new Object[]{mockProduct, 50L, new BigDecimal("50000000")};
        when(orderItemRepository.findTopSellingProducts(any(Pageable.class)))
                .thenReturn(Collections.singletonList(row));

        List<TopProductResponse> response = dashboardService.getTopProducts(10);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getProductId());
        assertEquals("iPhone 15", response.get(0).getProductName());
        assertEquals(50L, response.get(0).getTotalSold());
        assertEquals(new BigDecimal("50000000"), response.get(0).getRevenue());
    }

    @Test
    @DisplayName("Nên lấy danh sách đơn hàng gần đây thành công")
    void getRecentOrders_Success() {
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setRecipientName("Nguyen Van A");
        mockOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(mockOrder));

        List<RecentOrderResponse> response = dashboardService.getRecentOrders(5);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getOrderId());
        assertEquals("Nguyen Van A", response.get(0).getCustomerName());
        assertEquals(OrderStatus.DELIVERED, response.get(0).getStatus());
        assertEquals("Đã giao hàng", response.get(0).getStatusLabel());
    }
}