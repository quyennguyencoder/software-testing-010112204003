package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.order.CreateOrderRequest;
import com.phonehub.backend.dto.response.order.CreateOrderResponse;
import com.phonehub.backend.dto.response.order.OrderResponse;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.enums.PaymentMethod;
import com.phonehub.backend.service.intf.IOrderService;
import com.phonehub.backend.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IOrderService orderService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setMessageConverters(TestPageSerializer.createPageMessageConverter())
                .build();
    }

    @Test
    public void getOrderById_ShouldReturnOrder() throws Exception {
        Long userId = 1L;
        Long orderId = 10L;
        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD123")
                .status(OrderStatus.PENDING)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    public void createOrder_ShouldReturnCreatedOrder() throws Exception {
        Long userId = 1L;
        com.phonehub.backend.dto.request.order.OrderItemRequest item = 
                com.phonehub.backend.dto.request.order.OrderItemRequest.builder()
                        .productId(1L)
                        .quantity(1)
                        .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .email("test@example.com")
                .recipientName("John Doe")
                .phoneNumber("0987654321")
                .shippingAddress("123 Street")
                .paymentMethod(PaymentMethod.COD)
                .items(Collections.singletonList(item))
                .build();

        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(10L)
                .orderCode("OD123")
                .status(OrderStatus.PENDING)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.createOrder(any(CreateOrderRequest.class), eq(userId), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(10L));
    }

    @Test
    public void getMyOrders_ShouldReturnOrdersList() throws Exception {
        Long userId = 1L;
        OrderResponse response = OrderResponse.builder().id(10L).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getMyOrders(userId)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getMyOrdersPaginated_ShouldReturnPagedOrders() throws Exception {
        Long userId = 1L;
        OrderResponse response = OrderResponse.builder().id(10L).build();
        Page<OrderResponse> pagedResponse = new PageImpl<>(Collections.singletonList(response));

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getMyOrdersWithPagination(eq(userId), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/orders/my-orders/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getMyOrdersPaginated_SortAsc_ShouldReturnPagedOrders() throws Exception {
        Long userId = 1L;
        OrderResponse response = OrderResponse.builder().id(11L).build();
        Page<OrderResponse> pagedResponse = new PageImpl<>(Collections.singletonList(response));

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getMyOrdersWithPagination(eq(userId), any(Pageable.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/orders/my-orders/paginated")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getMyOrdersByStatus_ShouldReturnList() throws Exception {
        Long userId = 1L;
        OrderResponse response = OrderResponse.builder().id(10L).build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getMyOrdersByStatus(userId, OrderStatus.DELIVERED)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v1/orders/my-orders/by-status").param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void getMyOrdersCount_ShouldReturnCount() throws Exception {
        Long userId = 1L;

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getMyOrdersCount(userId)).thenReturn(5L);

        mockMvc.perform(get("/api/v1/orders/my-orders/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5L));
    }

    @Test
    public void cancelMyOrder_ShouldReturnSuccess() throws Exception {
        Long userId = 1L;
        Long orderId = 10L;

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        doNothing().when(orderService).cancelMyOrder(orderId, userId);

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void canCancelOrder_ShouldReturnResult() throws Exception {
        Long userId = 1L;
        Long orderId = 10L;

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/orders/{orderId}/can-cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    public void canCancelOrder_ShouldReturnFalse() throws Exception {
        Long userId = 1L;
        Long orderId = 11L;

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/can-cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnInfo() throws Exception {
        Long userId = 1L;
        Long orderId = 10L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD123")
                .status(OrderStatus.PENDING)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(orderId));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnConfirmedStatusDisplayAndDescription() throws Exception {
        Long userId = 1L;
        Long orderId = 11L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD124")
                .status(OrderStatus.CONFIRMED)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDisplay").value("Đã xác nhận"))
                .andExpect(jsonPath("$.data.statusDescription").value("Đơn hàng đã được xác nhận và đang chuẩn bị hàng hóa."))
                .andExpect(jsonPath("$.data.canCancel").value(false));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnShippingStatusDisplayAndDescription() throws Exception {
        Long userId = 1L;
        Long orderId = 12L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD125")
                .status(OrderStatus.SHIPPING)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDisplay").value("Đang giao hàng"))
                .andExpect(jsonPath("$.data.statusDescription").value("Đơn hàng đang được giao đến địa chỉ của bạn."));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnShippedStatusDisplayAndDescription() throws Exception {
        Long userId = 1L;
        Long orderId = 13L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD126")
                .status(OrderStatus.SHIPPED)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDisplay").value("Đang giao hàng"))
                .andExpect(jsonPath("$.data.statusDescription").value("Đơn hàng đang được giao đến địa chỉ của bạn."));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnDeliveredStatusDisplayAndDescription() throws Exception {
        Long userId = 1L;
        Long orderId = 14L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD127")
                .status(OrderStatus.DELIVERED)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDisplay").value("Đã giao hàng"))
                .andExpect(jsonPath("$.data.statusDescription").value("Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng!"));
    }

    @Test
    public void getOrderStatusInfo_ShouldReturnCancelledStatusDisplayAndDescription() throws Exception {
        Long userId = 1L;
        Long orderId = 15L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .orderCode("OD128")
                .status(OrderStatus.CANCELLED)
                .build();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(orderService.getOrderById(orderId, userId)).thenReturn(orderResponse);
        when(orderService.canCancelOrder(orderId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/orders/{orderId}/status-info", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statusDisplay").value("Đã hủy"))
                .andExpect(jsonPath("$.data.statusDescription").value("Đơn hàng đã bị hủy."));
    }
}
