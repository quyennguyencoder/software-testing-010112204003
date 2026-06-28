package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.response.order.AdminOrderDetailResponse;
import com.phonehub.backend.dto.response.order.AdminOrderListResponse;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.service.intf.IAdminOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAdminOrderService adminOrderService;

    @InjectMocks
    private AdminOrderController adminOrderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminOrderController)
                .setMessageConverters(TestPageSerializer.createPageMessageConverter())
                .build();
    }

    @Test
    public void getAllOrders_ShouldReturnPagedOrders() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(10L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders")
                .param("search", "OD123")
                .param("status", "PENDING")
                .param("paymentMethod", "COD")
                .param("customerId", "1")
                .param("fromDate", "2026-01-01")
                .param("toDate", "2026-12-31")
                .param("minAmount", "100")
                .param("maxAmount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_InvalidDate_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                .param("fromDate", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void getAllOrders_InvalidAmount_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                .param("minAmount", "invalid-amount"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void getAllOrders_InvalidPaymentMethod_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                .param("paymentMethod", "invalid-method"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void getOrderDetail_ShouldReturnOrderDetail() throws Exception {
        Long orderId = 10L;
        AdminOrderDetailResponse response = new AdminOrderDetailResponse();
        response.setId(orderId);

        when(adminOrderService.getOrderDetail(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    public void updateOrderStatus_ShouldReturnUpdatedOrder() throws Exception {
        Long orderId = 10L;
        AdminOrderDetailResponse response = new AdminOrderDetailResponse();
        response.setId(orderId);
        response.setStatus(OrderStatus.CONFIRMED);

        when(adminOrderService.updateOrderStatus(eq(orderId), eq(OrderStatus.CONFIRMED), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/orders/{orderId}/status", orderId)
                .param("newStatus", "CONFIRMED")
                .param("adminNote", "Approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getOrderStatistics_ShouldReturnStatistics() throws Exception {
        when(adminOrderService.getOrderStatistics()).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/v1/admin/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getRecentOrders_ShouldReturnList() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(10L);

        when(adminOrderService.getRecentOrders(5)).thenReturn(Collections.singletonList(listResponse));

        mockMvc.perform(get("/api/v1/admin/orders/recent").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAvailableTransitions_ShouldReturnTransitions() throws Exception {
        Long orderId = 10L;

        when(adminOrderService.getAvailableStatusTransitions(orderId)).thenReturn(Collections.singletonList(OrderStatus.CONFIRMED));

        mockMvc.perform(get("/api/v1/admin/orders/{orderId}/available-transitions", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getOrdersByCustomer_ShouldReturnList() throws Exception {
        Long customerId = 1L;
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(10L);

        when(adminOrderService.getOrdersByCustomer(customerId)).thenReturn(Collections.singletonList(listResponse));

        mockMvc.perform(get("/api/v1/admin/orders/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_MinAmountZero_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(11L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("minAmount", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_MaxAmountZero_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(12L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("maxAmount", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_EmptyPaymentMethod_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(13L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        // send empty paymentMethod param to trigger the "no payment method" branch
        mockMvc.perform(get("/api/v1/admin/orders").param("paymentMethod", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_NoFilters_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(21L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_SortAsc_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(22L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("sortDirection", "asc").param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_MinAmountNegative_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(23L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("minAmount", "-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_MaxAmountNegative_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(24L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("maxAmount", "-50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_PaymentMethodLowercase_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(25L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("paymentMethod", "cod"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_EmptyFromDate_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(26L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("fromDate", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_EmptyToDate_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(27L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("toDate", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_EmptyMinAmount_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(28L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("minAmount", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getAllOrders_EmptyMaxAmount_Ignored_ShouldReturnOk() throws Exception {
        AdminOrderListResponse listResponse = new AdminOrderListResponse();
        listResponse.setId(29L);
        Page<AdminOrderListResponse> pagedResponse = new PageImpl<>(Collections.singletonList(listResponse));

        when(adminOrderService.getAllOrders(any(), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/admin/orders").param("maxAmount", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }
}
