package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.order.TrackOrderRequest;
import com.phonehub.backend.dto.response.order.PublicOrderTrackingResponse;
import com.phonehub.backend.entity.Order;
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

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PublicOrderServiceImpl publicOrderService;

    @Test
    @DisplayName("Nên tra cứu đơn hàng thành công")
    void trackOrder_Success() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("test@example.com");

        Order mockOrder = new Order();
        mockOrder.setOrderCode("ORD123");
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByOrderCodeAndEmail("ORD123", "test@example.com"))
                .thenReturn(Optional.of(mockOrder));

        PublicOrderTrackingResponse response = publicOrderService.trackOrder(request);

        assertNotNull(response);
        assertEquals("ORD123", response.getOrderCode());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Nên tra cứu đơn hàng với trim và normalize mã/email")
    void trackOrder_UsesTrimAndNormalize() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode(" ord123 ");
        request.setEmail(" TEST@example.com ");

        Order mockOrder = new Order();
        mockOrder.setOrderCode("ORD123");
        mockOrder.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findByOrderCodeAndEmail("ORD123", "test@example.com"))
                .thenReturn(Optional.of(mockOrder));

        PublicOrderTrackingResponse response = publicOrderService.trackOrder(request);

        assertNotNull(response);
        assertEquals("ORD123", response.getOrderCode());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("Nên tra cứu đơn hàng với email có phần username chỉ 2 ký tự")
    void trackOrder_UsesShortUsernameMaskEmailBranch() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("ab@example.com");

        Order mockOrder = new Order();
        mockOrder.setOrderCode("ORD123");
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByOrderCodeAndEmail("ORD123", "ab@example.com"))
                .thenReturn(Optional.of(mockOrder));

        PublicOrderTrackingResponse response = publicOrderService.trackOrder(request);

        assertNotNull(response);
        assertEquals("ORD123", response.getOrderCode());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi orderCode trống")
    void trackOrder_ThrowsException_WhenOrderCodeEmpty() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("   ");
        request.setEmail("test@example.com");

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi email không đúng định dạng")
    void trackOrder_ThrowsException_WhenEmailInvalidFormat() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("invalid-email");

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi email thiếu dấu chấm sau @")
    void trackOrder_ThrowsException_WhenEmailMissingDot() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("test@example");

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi email trống")
    void trackOrder_ThrowsException_WhenEmailEmpty() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("");

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi orderCode null")
    void trackOrder_ThrowsException_WhenOrderCodeNull() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode(null);
        request.setEmail("test@example.com");

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi email null")
    void trackOrder_ThrowsException_WhenEmailNull() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail(null);

        assertThrows(BadRequestException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném ResourceNotFoundException khi không tìm thấy đơn")
    void trackOrder_ThrowsException_WhenOrderNotFound() {
        TrackOrderRequest request = new TrackOrderRequest();
        request.setOrderCode("ORD123");
        request.setEmail("notfound@example.com");

        when(orderRepository.findByOrderCodeAndEmail("ORD123", "notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicOrderService.trackOrder(request));
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi quickTrackByCode với orderCode trống")
    void quickTrackByCode_ThrowsException_WhenOrderCodeEmpty() {
        assertThrows(BadRequestException.class, () -> publicOrderService.quickTrackByCode("   "));
    }

    @Test
    @DisplayName("Nên tra cứu nhanh đơn hàng thành công và ẩn thông tin nhạy cảm")
    void quickTrackByCode_Success() {
        Order mockOrder = new Order();
        mockOrder.setOrderCode("ORD123");
        mockOrder.setRecipientName("Nguyen Van A");
        mockOrder.setPhoneNumber("0123456789");
        mockOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByOrderCode("ORD123"))
                .thenReturn(Optional.of(mockOrder));

        PublicOrderTrackingResponse response = publicOrderService.quickTrackByCode("ORD123");

        assertNotNull(response);
        assertEquals("ORD123", response.getOrderCode());
        assertEquals("***", response.getRecipientName());
        assertEquals("***", response.getMaskedPhoneNumber());
        assertNull(response.getTotalAmount());
        assertEquals("Để xem đầy đủ thông tin, vui lòng nhập email khi đặt hàng.", response.getCustomerMessage());
    }

    @Test
    @DisplayName("Nên ném BadRequestException khi quickTrackByCode với orderCode null")
    void quickTrackByCode_ThrowsException_WhenOrderCodeNull() {
        assertThrows(BadRequestException.class, () -> publicOrderService.quickTrackByCode(null));
    }

    @Test
    @DisplayName("Nên ném ResourceNotFoundException khi quickTrackByCode không tìm thấy đơn")
    void quickTrackByCode_ThrowsException_WhenOrderNotFound() {
        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicOrderService.quickTrackByCode("ORD123"));
    }

    @Test
    @DisplayName("Nên validateOrderAccess trả về true với mã và email đúng")
    void validateOrderAccess_ReturnsTrue_WhenValid() {
        when(orderRepository.existsByOrderCodeAndEmail("ORD123", "test@example.com")).thenReturn(true);

        assertTrue(publicOrderService.validateOrderAccess(" ord123 ", " TEST@example.com "));
    }

    @Test
    @DisplayName("Nên validateOrderAccess trả về false khi mã hoặc email trống")
    void validateOrderAccess_ReturnsFalse_WhenCodeOrEmailEmpty() {
        assertFalse(publicOrderService.validateOrderAccess("", "test@example.com"));
        assertFalse(publicOrderService.validateOrderAccess("ORD123", ""));
        assertFalse(publicOrderService.validateOrderAccess(null, "test@example.com"));
        assertFalse(publicOrderService.validateOrderAccess("ORD123", null));
    }

    @Test
    @DisplayName("Nên validateOrderAccess trả về false khi repository throws exception")
    void validateOrderAccess_ReturnsFalse_WhenRepositoryThrows() {
        when(orderRepository.existsByOrderCodeAndEmail(any(), any())).thenThrow(new RuntimeException("DB error"));

        assertFalse(publicOrderService.validateOrderAccess("ORD123", "test@example.com"));
    }

    @Test
    @DisplayName("Nên lấy thống kê đơn hàng thành công")
    void getTrackingStatistics_Success() {
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(80L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(10L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPING)).thenReturn(10L);

        Object result = publicOrderService.getTrackingStatistics();

        assertTrue(result instanceof Map);
        Map<String, Object> stats = (Map<String, Object>) result;
        assertEquals(100L, stats.get("totalOrders"));
        assertEquals(80L, stats.get("deliveredOrders"));
        assertEquals(80.0, stats.get("deliveryRate"));
    }

    @Test
    @DisplayName("Nên trả về deliveryRate = 0 khi không có đơn hàng")
    void getTrackingStatistics_WhenNoOrders_ReturnsZeroRate() {
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.SHIPPING)).thenReturn(0L);

        Object result = publicOrderService.getTrackingStatistics();

        assertTrue(result instanceof Map);
        Map<String, Object> stats = (Map<String, Object>) result;
        assertEquals(0L, stats.get("totalOrders"));
        assertEquals(0.0, stats.get("deliveryRate"));
    }

    @Test
    @DisplayName("Nên trả về map lỗi khi thống kê đơn hàng gặp exception")
    void getTrackingStatistics_WhenRepositoryThrows_ReturnsErrorMap() {
        when(orderRepository.count()).thenThrow(new RuntimeException("DB error"));

        Object result = publicOrderService.getTrackingStatistics();

        assertTrue(result instanceof Map);
        Map<String, Object> stats = (Map<String, Object>) result;
        assertEquals("Unable to fetch statistics", stats.get("error"));
    }
}
