package com.phonehub.backend.controller;

import com.phonehub.backend.dto.response.dashboard.*;
import com.phonehub.backend.enums.DashboardPeriod;
import com.phonehub.backend.enums.RegistrationPeriod;
import com.phonehub.backend.service.intf.IDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IDashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    public void getOverview_ShouldReturnOverview() throws Exception {
        DashboardOverviewResponse response = DashboardOverviewResponse.builder()
                .totalRevenue(java.math.BigDecimal.valueOf(1000000))
                .totalOrders(100L)
                .totalProducts(50L)
                .totalUsers(20L)
                .build();

        when(dashboardService.getOverview()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrders").value(100L));
    }

    @Test
    public void getRevenueChart_ShouldReturnRevenueChart() throws Exception {
        RevenueChartResponse response = RevenueChartResponse.builder()
                .labels(Collections.singletonList("2026-05-30"))
                .values(Collections.singletonList(java.math.BigDecimal.valueOf(50000)))
                .build();

        when(dashboardService.getRevenueChart(any(DashboardPeriod.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/revenue-chart")
                .param("period", "THIRTY_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getOrderStatusChart_ShouldReturnOrderStatusChart() throws Exception {
        OrderStatusChartResponse response = OrderStatusChartResponse.builder()
                .labels(Collections.singletonList("DELIVERED"))
                .values(Collections.singletonList(10L))
                .percentages(Collections.singletonList(100.0))
                .build();

        when(dashboardService.getOrderStatusChart()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/order-status-chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getUserRegistrationChart_ShouldReturnUserRegistrationChart() throws Exception {
        UserRegistrationChartResponse response = UserRegistrationChartResponse.builder()
                .labels(Collections.singletonList("2026-05-30"))
                .values(Collections.singletonList(5L))
                .build();

        when(dashboardService.getUserRegistrationChart(any(RegistrationPeriod.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard/user-registration-chart")
                .param("period", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getTopProducts_ShouldReturnTopProducts() throws Exception {
        TopProductResponse product = TopProductResponse.builder()
                .productId(1L)
                .productName("iPhone 15")
                .totalSold(20L)
                .revenue(java.math.BigDecimal.valueOf(200000))
                .build();

        when(dashboardService.getTopProducts(5)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/v1/admin/dashboard/top-products")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getRecentOrders_ShouldReturnRecentOrders() throws Exception {
        RecentOrderResponse order = RecentOrderResponse.builder()
                .orderId(100L)
                .customerName("John Doe")
                .totalAmount(java.math.BigDecimal.valueOf(15000))
                .status(com.phonehub.backend.enums.OrderStatus.DELIVERED)
                .build();

        when(dashboardService.getRecentOrders(10)).thenReturn(Collections.singletonList(order));

        mockMvc.perform(get("/api/v1/admin/dashboard/recent-orders")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
        LowStockProductResponse product = LowStockProductResponse.builder()
                .productId(2L)
                .productName("Samsung S24")
                .stockQuantity(3)
                .build();

        when(dashboardService.getLowStockProducts(10)).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/api/v1/admin/dashboard/low-stock-products")
                .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }
}