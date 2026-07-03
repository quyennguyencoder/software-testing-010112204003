package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.response.dashboard.*;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.enums.RegistrationPeriod;
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
import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.entity.Category;
import com.phonehub.backend.entity.Brand;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.enums.DashboardPeriod;
import com.phonehub.backend.enums.RegistrationPeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    @Test
void getRevenueChart_Success() {

    Order order = new Order();
    order.setCreatedAt(LocalDateTime.now());
    order.setTotalAmount(BigDecimal.valueOf(100));

    when(orderRepository.findByCreatedAtBetweenAndStatus(
            any(),
            any(),
            eq(OrderStatus.DELIVERED)))
            .thenReturn(List.of(order));

    RevenueChartResponse response =
            dashboardService.getRevenueChart(DashboardPeriod.SEVEN_DAYS);

    assertNotNull(response);
    assertEquals(BigDecimal.valueOf(100), response.getTotal());

}
@Test
void getRevenueChart_Empty() {

    when(orderRepository.findByCreatedAtBetweenAndStatus(
            any(),
            any(),
            any()))
            .thenReturn(List.of());

    RevenueChartResponse response =
            dashboardService.getRevenueChart(DashboardPeriod.SEVEN_DAYS);

    assertEquals(BigDecimal.ZERO, response.getTotal());

}
@Test
@DisplayName("Should get revenue chart successfully")
void getRevenueChart_Success2() {

    Order order1 = new Order();
    order1.setCreatedAt(LocalDateTime.now().minusDays(1));
    order1.setTotalAmount(BigDecimal.valueOf(100));

    Order order2 = new Order();
    order2.setCreatedAt(LocalDateTime.now().minusDays(1));
    order2.setTotalAmount(BigDecimal.valueOf(200));

    Order order3 = new Order();
    order3.setCreatedAt(LocalDateTime.now());
    order3.setTotalAmount(BigDecimal.valueOf(300));

    when(orderRepository.findByCreatedAtBetweenAndStatus(
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(OrderStatus.DELIVERED)))
            .thenReturn(List.of(order1, order2, order3));

    RevenueChartResponse response =
            dashboardService.getRevenueChart(DashboardPeriod.SEVEN_DAYS);

    assertNotNull(response);
    assertEquals("SEVEN_DAYS", response.getPeriod());
    assertEquals(BigDecimal.valueOf(600), response.getTotal());
    assertEquals(7, response.getLabels().size());
    assertEquals(7, response.getValues().size());

    verify(orderRepository).findByCreatedAtBetweenAndStatus(
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(OrderStatus.DELIVERED));
}

@Test
@DisplayName("Should return empty revenue chart")
void getRevenueChart_Empty2() {

    when(orderRepository.findByCreatedAtBetweenAndStatus(
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(OrderStatus.DELIVERED)))
            .thenReturn(List.of());

    RevenueChartResponse response =
            dashboardService.getRevenueChart(DashboardPeriod.SEVEN_DAYS);

    assertNotNull(response);
    assertEquals(BigDecimal.ZERO, response.getTotal());
    assertEquals(7, response.getLabels().size());
    assertEquals(7, response.getValues().size());
}
@Test
@DisplayName("Should get user registration chart successfully")
void getUserRegistrationChart_Success() {

    User user1 = User.builder()
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();

    User user2 = User.builder()
            .createdAt(LocalDateTime.now())
            .build();

    when(userRepository.findByCreatedAtBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)))
            .thenReturn(List.of(user1, user2));

    UserRegistrationChartResponse response =
            dashboardService.getUserRegistrationChart(RegistrationPeriod.WEEKLY);

    assertNotNull(response);
    assertEquals(RegistrationPeriod.WEEKLY.name(), response.getPeriod());
    assertEquals(2L, response.getTotal());
    assertEquals(7, response.getLabels().size());
    assertEquals(7, response.getValues().size());

    verify(userRepository).findByCreatedAtBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class));
}

@Test
@DisplayName("Should return empty registration chart")
void getUserRegistrationChart_Empty() {

    when(userRepository.findByCreatedAtBetween(
            any(LocalDateTime.class),
            any(LocalDateTime.class)))
            .thenReturn(List.of());

    UserRegistrationChartResponse response =
            dashboardService.getUserRegistrationChart(RegistrationPeriod.WEEKLY);

    assertNotNull(response);
    assertEquals(0L, response.getTotal());
    assertEquals(7, response.getLabels().size());
    assertEquals(7, response.getValues().size());
}
@Test
@DisplayName("Should get low stock products successfully")
void getLowStockProducts_Success() {

    Category category = Category.builder()
            .name("Phone")
            .build();

    Brand brand = Brand.builder()
            .name("Apple")
            .build();

    ProductTemplate t1 = ProductTemplate.builder()
            .status(true)
            .stockQuantity(3)
            .build();

    ProductTemplate t2 = ProductTemplate.builder()
            .status(true)
            .stockQuantity(2)
            .build();

    Product product = Product.builder()
            .id(1L)
            .name("iPhone 15")
            .thumbnailUrl("img.jpg")
            .status(true)
            .category(category)
            .brand(brand)
            .build();

    product.addTemplate(t1);
    product.addTemplate(t2);

    when(productRepository
            .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(5))
            .thenReturn(List.of(product));

    List<LowStockProductResponse> result =
            dashboardService.getLowStockProducts(5);

    assertEquals(1, result.size());

    LowStockProductResponse response = result.get(0);

    assertEquals(1L, response.getProductId());
    assertEquals("iPhone 15", response.getProductName());
    assertEquals(5, response.getStockQuantity());
    assertEquals("Phone", response.getCategoryName());
    assertEquals("Apple", response.getBrandName());

    verify(productRepository)
            .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(5);
}

@Test
@DisplayName("Should return empty low stock list")
void getLowStockProducts_Empty() {

    when(productRepository
            .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(5))
            .thenReturn(List.of());

    List<LowStockProductResponse> result =
            dashboardService.getLowStockProducts(5);

    assertTrue(result.isEmpty());
}

@Test
@DisplayName("Should set threshold to zero when negative")
void getLowStockProducts_NegativeThreshold() {

    when(productRepository
            .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(0))
            .thenReturn(List.of());

    dashboardService.getLowStockProducts(-10);

    verify(productRepository)
            .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(0);
}

}