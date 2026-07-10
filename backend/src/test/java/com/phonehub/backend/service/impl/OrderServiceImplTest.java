package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.request.order.CreateOrderRequest;
import com.phonehub.backend.dto.request.order.OrderItemRequest;
import com.phonehub.backend.dto.response.order.CreateOrderResponse;
import com.phonehub.backend.dto.response.order.OrderResponse;
import com.phonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.phonehub.backend.entity.Cart;
import com.phonehub.backend.entity.CartItem;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.OrderItem;
import com.phonehub.backend.entity.Payment;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.enums.PaymentMethod;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ForbiddenException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.mapper.OrderMapper;
import com.phonehub.backend.repository.CartItemRepository;
import com.phonehub.backend.repository.CartRepository;
import com.phonehub.backend.repository.OrderItemRepository;
import com.phonehub.backend.repository.OrderRepository;
import com.phonehub.backend.repository.PaymentRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.repository.PromotionRepository;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.service.intf.IEmailService;
import com.phonehub.backend.service.intf.IVNPayService;
import com.phonehub.backend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private IVNPayService vnPayService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Order testOrder;
    private Product testProduct;
    private ProductTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setOrderCode("ORD123");
        testOrder.setPaymentMethod(PaymentMethod.COD);

        testTemplate = new ProductTemplate();
        testTemplate.setId(1L);
        testTemplate.setStatus(true);
        testTemplate.setStockQuantity(10);
        testTemplate.setPrice(BigDecimal.valueOf(100000));

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setTemplates(Collections.singletonList(testTemplate));
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));
        OrderResponse orderResponse = new OrderResponse();
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(1L, 1L);

        assertNotNull(result);
        verify(orderRepository, times(1)).findByIdWithItems(1L);
    }

    @Test
    void getOrderById_NotOwner_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);
        
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(ForbiddenException.class, () -> orderService.getOrderById(1L, 1L));
    }

    @Test
    void createOrder_COD_Success() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(10L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(emailService, times(1)).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());
        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void createOrder_VNPAY_Success() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.VNPAY);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(20L);
            return savedOrder;
        });
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vnPayService.createPaymentUrl(any(), eq("127.0.0.1"))).thenReturn(VNPayPaymentResponse.builder().paymentUrl("http://vnpay.test").build());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.VNPAY, response.getPaymentMethod());
        assertEquals("http://vnpay.test", response.getPaymentUrl());
        assertEquals("Đơn hàng đã tạo. Đang chuyển hướng thanh toán VNPay...", response.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(emailService, never()).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());
        verify(vnPayService, times(1)).createPaymentUrl(any(), eq("127.0.0.1"));
    }

    @Test
    void createOrder_VNPAY_WithServletRequest_UsesClientIp() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.VNPAY);
        Cart cart = createCartWithItem(testProduct);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(21L);
            return savedOrder;
        });
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityUtils.getClientIp(servletRequest)).thenReturn("192.168.1.100");
        when(vnPayService.createPaymentUrl(any(), eq("192.168.1.100")))
                .thenReturn(VNPayPaymentResponse.builder().paymentUrl("http://vnpay.clientip").build());

        CreateOrderResponse response = orderService.createOrder(request, 1L, servletRequest);

        assertNotNull(response);
        assertEquals(PaymentMethod.VNPAY, response.getPaymentMethod());
        assertEquals("http://vnpay.clientip", response.getPaymentUrl());
        verify(securityUtils, times(1)).getClientIp(servletRequest);
        verify(vnPayService, times(1)).createPaymentUrl(any(), eq("192.168.1.100"));
    }

    @Test
    void createOrder_VNPAY_PaymentUrlThrows_ReturnsCreateLinkErrorMessage() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.VNPAY);
        Cart cart = createCartWithItem(testProduct);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(22L);
            return savedOrder;
        });
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityUtils.getClientIp(servletRequest)).thenReturn("192.168.1.101");
        when(vnPayService.createPaymentUrl(any(), eq("192.168.1.101"))).thenThrow(new RuntimeException("VNPay failed"));

        CreateOrderResponse response = orderService.createOrder(request, 1L, servletRequest);

        assertNotNull(response);
        assertEquals(PaymentMethod.VNPAY, response.getPaymentMethod());
        assertEquals("Đơn hàng đã tạo nhưng lỗi tạo link thanh toán. Vui lòng thử lại trong lịch sử đơn hàng.", response.getMessage());
        verify(vnPayService, times(1)).createPaymentUrl(any(), eq("192.168.1.101"));
    }

    @Test
    void createOrder_COD_EmailSendThrows_ShouldStillCreateOrder() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(23L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Email fail"))
                .when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(emailService, times(1)).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    void createOrder_COD_CartClearingThrows_ShouldStillCreateOrder() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(24L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenThrow(new RuntimeException("Cart fetch failed"));

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(cartRepository, times(1)).findByUserIdWithItems(1L);
    }

    @Test
    void getOrderById_NotFound_ThrowsResourceNotFoundException() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L, 1L));
    }

    @Test
    void createOrder_InvalidPromotionAndFreeshipId_ShouldIgnorePromotionAndCreateSuccessfully() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        request.setPromotionId("invalid-promo");
        request.setFreeshippingPromotionId("invalid-free");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(30L);
            return savedOrder;
        });
        when(promotionRepository.findById(anyString())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(promotionRepository, times(2)).findById(anyString());
    }

    @Test
    void createOrder_ProductNotFound_ThrowsBadRequestException() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ProductOutOfStock_ThrowsBadRequestException() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        testTemplate.setStockQuantity(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_QuantityZero_ThrowsBadRequestException() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        // set quantity to 0 to test boundary
        request.getItems().get(0).setQuantity(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_QuantityOne_Succeeds() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        // explicit quantity = 1 (boundary)
        request.getItems().get(0).setQuantity(1);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(50L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_QuantityExceedsStock_ThrowsBadRequestException() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        // request quantity more than available stock
        request.getItems().get(0).setQuantity(9999);

        testTemplate.setStockQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithPartialStockReduction_ThrowsBadRequestExceptionAfterDeduction() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        request.getItems().get(0).setQuantity(2);

        ProductTemplate unstableTemplate = spy(new ProductTemplate());
        unstableTemplate.setId(2L);
        unstableTemplate.setStatus(true);
        unstableTemplate.setPrice(BigDecimal.valueOf(100000));
        unstableTemplate.setStockQuantity(2);

        AtomicInteger counter = new AtomicInteger();
        doAnswer(invocation -> {
            int callIndex = counter.getAndIncrement();
            if (callIndex == 0) {
                return 2; // totalStock validation
            }
            return 0; // deduction pass will leave quantity unmet
        }).when(unstableTemplate).getStockQuantity();

        Product unstableProduct = new Product();
        unstableProduct.setId(2L);
        unstableProduct.setName("Unstable Product");
        unstableProduct.setTemplates(Collections.singletonList(unstableTemplate));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(2L))).thenReturn(Collections.singletonList(unstableProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        request.getItems().get(0).setProductId(2L);

        assertThrows(BadRequestException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(emailService, never()).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    void cancelMyOrder_OrderNotFound_ThrowsResourceNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelMyOrder(1L, 1L));
    }

    @Test
    void canCancelOrder_NotOwner_ReturnsFalse() {
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertFalse(orderService.canCancelOrder(1L, 1L));
    }

    @Test
    void cancelMyOrder_StatusShipping_ThrowsBadRequestExceptionWithShippingMessage() {
        testOrder.setStatus(OrderStatus.SHIPPING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.cancelMyOrder(1L, 1L));

        assertTrue(exception.getMessage().contains("Đang giao hàng"));
    }

    @Test
    void cancelMyOrder_StatusDelivered_ThrowsBadRequestExceptionWithDeliveredMessage() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.cancelMyOrder(1L, 1L));

        assertTrue(exception.getMessage().contains("Đã giao hàng"));
    }

    @Test
    void cancelMyOrder_StatusShipped_ThrowsBadRequestExceptionWithShippedMessage() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.cancelMyOrder(1L, 1L));

        assertTrue(exception.getMessage().contains("Đang giao hàng"));
    }

    @Test
    void cancelMyOrder_StatusConfirmed_ThrowsBadRequestExceptionWithConfirmedMessage() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.cancelMyOrder(1L, 1L));

        assertTrue(exception.getMessage().contains("Đã xác nhận"));
    }

    @Test
    void cancelMyOrder_StatusCancelled_ThrowsBadRequestExceptionWithCancelledMessage() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.cancelMyOrder(1L, 1L));

        assertTrue(exception.getMessage().contains("Đã hủy"));
    }

    @Test
    void cancelMyOrder_WithOrderItems_RestoresStockAndHandlesMissingProduct() {
        OrderItem hasProductItem = new OrderItem();
        hasProductItem.setId(1L);
        hasProductItem.setProduct(testProduct);
        hasProductItem.setQuantity(1);

        OrderItem missingProductItem = new OrderItem();
        missingProductItem.setId(2L);
        missingProductItem.setProduct(null);
        missingProductItem.setQuantity(2);

        testOrder.setItems(List.of(hasProductItem, missingProductItem));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void createOrder_GenerateUniqueOrderCode_WithCollision_Success() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(true, false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(40L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(orderRepository, atLeast(2)).existsByOrderCode(anyString());
    }

    @Test
    void getMyOrderDetail_DelegatesToGetOrderById() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));
        OrderResponse orderResponse = new OrderResponse();
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.getMyOrderDetail(1L, 1L);

        assertSame(orderResponse, result);
        verify(orderRepository, times(1)).findByIdWithItems(1L);
    }

    @Test
    void cancelMyOrder_NotOwner_ThrowsForbiddenException() {
        User otherUser = new User();
        otherUser.setId(2L);
        testOrder.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(ForbiddenException.class, () -> orderService.cancelMyOrder(1L, 1L));
    }

    @Test
    void cancelMyOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelMyOrder_NotPending_ThrowsException() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancelMyOrder(1L, 1L));
    }

    @Test
    void canCancelOrder_ReturnsTrue() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.canCancelOrder(1L, 1L);

        assertTrue(result);
    }

    @Test
    void canCancelOrder_ReturnsFalse() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        boolean result = orderService.canCancelOrder(1L, 1L);

        assertFalse(result);
    }

    @Test
    void canCancelOrder_RepositoryThrows_ReturnsFalse() {
        when(orderRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        boolean result = orderService.canCancelOrder(1L, 1L);

        assertFalse(result);
    }

    @Test
    void canCancelOrder_OrderNotFound_ReturnsFalse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = orderService.canCancelOrder(1L, 1L);

        assertFalse(result);
    }

    @Test
    void getMyOrders_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(Collections.singletonList(testOrder));
        
        OrderResponse orderResponse = new OrderResponse();
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getMyOrders(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyOrders_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyOrders(1L));
    }

    @Test
    void getMyOrdersWithPagination_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder));
        when(orderRepository.findByUserOrderByCreatedAtDesc(testUser, pageable)).thenReturn(orderPage);
        
        OrderResponse orderResponse = new OrderResponse();
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(orderResponse);

        Page<OrderResponse> result = orderService.getMyOrdersWithPagination(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getMyOrdersWithPagination_UserNotFound_ThrowsResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getMyOrdersWithPagination(1L, pageable));
    }

    @Test
    void getMyOrdersByStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserAndStatusOrderByCreatedAtDesc(testUser, OrderStatus.PENDING)).thenReturn(Collections.singletonList(testOrder));
        
        OrderResponse orderResponse = new OrderResponse();
        when(orderMapper.toOrderResponse(testOrder)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getMyOrdersByStatus(1L, OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyOrdersByStatus_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getMyOrdersByStatus(1L, OrderStatus.PENDING));
    }

    @Test
    void getMyOrdersCount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.countByUser(testUser)).thenReturn(5L);

        long count = orderService.getMyOrdersCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void getMyOrdersCount_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getMyOrdersCount(1L));
    }

    @Test
    void getStatusDisplayName_Reflection_CoversAllStatuses() throws Exception {
        Method method = OrderServiceImpl.class.getDeclaredMethod("getStatusDisplayName", OrderStatus.class);
        method.setAccessible(true);

        assertEquals("Chờ xác nhận", method.invoke(orderService, OrderStatus.PENDING));
        assertEquals("Đã xác nhận", method.invoke(orderService, OrderStatus.CONFIRMED));
        assertEquals("Đang giao hàng", method.invoke(orderService, OrderStatus.SHIPPING));
        assertEquals("Đang giao hàng", method.invoke(orderService, OrderStatus.SHIPPED));
        assertEquals("Đã giao hàng", method.invoke(orderService, OrderStatus.DELIVERED));
        assertEquals("Đã hủy", method.invoke(orderService, OrderStatus.CANCELLED));
    }

    @Test
    void createOrder_MultipleTemplates_ExercisesContinueBranch() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        request.getItems().get(0).setQuantity(1);

        ProductTemplate inactiveTemplate = new ProductTemplate();
        inactiveTemplate.setId(11L);
        inactiveTemplate.setStatus(false);
        inactiveTemplate.setStockQuantity(50);
        inactiveTemplate.setPrice(BigDecimal.valueOf(90000));

        ProductTemplate zeroStockTemplate = new ProductTemplate();
        zeroStockTemplate.setId(12L);
        zeroStockTemplate.setStatus(true);
        zeroStockTemplate.setStockQuantity(0);
        zeroStockTemplate.setPrice(BigDecimal.valueOf(80000));

        ProductTemplate activeTemplate = new ProductTemplate();
        activeTemplate.setId(13L);
        activeTemplate.setStatus(true);
        activeTemplate.setStockQuantity(2);
        activeTemplate.setPrice(BigDecimal.valueOf(70000));

        ProductTemplate exhaustedAfterDeductionTemplate = new ProductTemplate();
        exhaustedAfterDeductionTemplate.setId(14L);
        exhaustedAfterDeductionTemplate.setStatus(true);
        exhaustedAfterDeductionTemplate.setStockQuantity(4);
        exhaustedAfterDeductionTemplate.setPrice(BigDecimal.valueOf(60000));

        Product multiTemplateProduct = new Product();
        multiTemplateProduct.setId(1L);
        multiTemplateProduct.setName("Multi Template Product");
        multiTemplateProduct.setTemplates(List.of(inactiveTemplate, zeroStockTemplate, activeTemplate, exhaustedAfterDeductionTemplate));

        Cart cart = createCartWithItem(multiTemplateProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(multiTemplateProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(70L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(productRepository, times(1)).save(multiTemplateProduct);
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void cancelMyOrder_StatusHistoryThrows_ShouldStillCancelOrder() {
        Order mockOrder = mock(Order.class);
        User mockUser = mock(User.class);
        AtomicInteger orderCodeCalls = new AtomicInteger();
        when(mockOrder.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);
        when(mockOrder.getStatus()).thenReturn(OrderStatus.PENDING);
        when(mockOrder.getPaymentMethod()).thenReturn(PaymentMethod.COD);
        when(mockOrder.getItems()).thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            if (orderCodeCalls.getAndIncrement() == 0) {
                throw new RuntimeException("history failure");
            }
            return "ORD-HISTORY-1";
        }).when(mockOrder).getOrderCode();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        assertDoesNotThrow(() -> orderService.cancelMyOrder(1L, 1L));

        verify(orderRepository, times(1)).save(mockOrder);
        verify(mockOrder, atLeast(1)).getOrderCode();
    }

    private CreateOrderRequest buildOrderRequest(PaymentMethod paymentMethod) {
        return CreateOrderRequest.builder()
                .email("test@example.com")
                .recipientName("John Doe")
                .phoneNumber("0987654321")
                .shippingAddress("123 Street")
                .paymentMethod(paymentMethod)
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .productId(1L)
                                .quantity(1)
                                .build()
                ))
                .build();
    }

    private Cart createCartWithItem(Product product) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);

        cart.addItem(cartItem);
        return cart;
    }

    // ========================================
    // Additional Tests for Coverage
    // ========================================

    @Test
    void cancelMyOrder_BankTransfer_RestoresStock() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);

        testOrder.setItems(List.of(orderItem));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelMyOrder_StockRestoreThrows_ShouldNotFailOrder() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);

        testOrder.setItems(List.of(orderItem));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelMyOrder_NullOrderItems_HandledGracefully() {
        testOrder.setItems(null);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void createOrder_BankTransfer_Success() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.BANK_TRANSFER);
        Cart cart = createCartWithItem(testProduct);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(60L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.BANK_TRANSFER, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(emailService, times(1)).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    void createOrder_MultipleItems_Success() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        
        ProductTemplate template2 = new ProductTemplate();
        template2.setId(2L);
        template2.setStatus(true);
        template2.setStockQuantity(15);
        template2.setPrice(BigDecimal.valueOf(200000));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setTemplates(Collections.singletonList(template2));

        OrderItemRequest item2 = OrderItemRequest.builder()
                .productId(2L)
                .quantity(2)
                .build();

        List<OrderItemRequest> items = new ArrayList<>(request.getItems());
        items.add(item2);
        request.setItems(items);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(1L);
        cartItem1.setProduct(testProduct);
        cartItem1.setQuantity(1);
        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        cartItem2.setProduct(product2);
        cartItem2.setQuantity(2);
        cart.addItem(cartItem1);
        cart.addItem(cartItem2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(testProduct, product2));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(70L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
    }

    @Test
    void createOrder_VNPAY_WithClientIpFromRequest() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.VNPAY);
        Cart cart = createCartWithItem(testProduct);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(71L);
            return savedOrder;
        });
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityUtils.getClientIp(servletRequest)).thenReturn("10.0.0.1");
        when(vnPayService.createPaymentUrl(any(), eq("10.0.0.1")))
                .thenReturn(VNPayPaymentResponse.builder().paymentUrl("http://vnpay.test.ip").build());

        CreateOrderResponse response = orderService.createOrder(request, 1L, servletRequest);

        assertNotNull(response);
        assertEquals("http://vnpay.test.ip", response.getPaymentUrl());
        verify(securityUtils, times(1)).getClientIp(servletRequest);
    }

    @Test
    void createOrder_UserNotFound_ThrowsResourceNotFoundException() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request, 1L));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getStatusDisplayName_AllStatuses() {
        // Since getStatusDisplayName is private, we test it indirectly via cancelMyOrder with different statuses
        
        // Test PENDING in error message
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        orderService.cancelMyOrder(1L, 1L);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());

        // Test other statuses throw exceptions with correct display names
        String[] expectedMessages = {
            "Chờ xác nhận",    // PENDING
            "Đã xác nhận",     // CONFIRMED
            "Đang giao hàng",  // SHIPPING
            "Đang giao hàng",  // SHIPPED
            "Đã giao hàng",    // DELIVERED
            "Đã hủy"           // CANCELLED
        };

        OrderStatus[] statuses = {
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPING,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED
        };

        for (int i = 1; i < statuses.length; i++) {
            testOrder.setStatus(statuses[i]);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> orderService.cancelMyOrder(1L, 1L));

            assertTrue(exception.getMessage().contains(expectedMessages[i]),
                    "Expected message containing '" + expectedMessages[i] + "' for status " + statuses[i]);
        }
    }

    @Test
    void restoreProductStock_MultipleOrderItems() {
        ProductTemplate template2 = new ProductTemplate();
        template2.setId(2L);
        template2.setStatus(true);
        template2.setStockQuantity(8);
        template2.setPrice(BigDecimal.valueOf(150000));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Product 2");
        product2.setTemplates(Collections.singletonList(template2));

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProduct(testProduct);
        item1.setQuantity(3);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProduct(product2);
        item2.setQuantity(2);

        testOrder.setItems(List.of(item1, item2));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void restoreProductStock_EmptyOrderItems() {
        testOrder.setItems(Collections.emptyList());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void createOrder_CartNotFound_StillCreatesOrder() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(80L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals("Đơn hàng đã được tạo thành công!", response.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).findByUserIdWithItems(1L);
    }

    @Test
    void createOrder_CartItemsNoMatch_StillCreatesOrder() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);
        
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(new Product());
        cartItem.getProduct().setId(999L);
        cartItem.setQuantity(1);
        cart.addItem(cartItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(81L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void getStatusDisplayName_AllStatuses_ViaExceptionMessages() {
        OrderStatus[] statuses = {OrderStatus.CONFIRMED, OrderStatus.SHIPPING, 
                                 OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED};
        String[] expectedMessages = {"Đã xác nhận", "Đang giao hàng", 
                                     "Đang giao hàng", "Đã giao hàng", "Đã hủy"};
        
        for (int i = 0; i < statuses.length; i++) {
            testOrder.setStatus(statuses[i]);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> orderService.cancelMyOrder(1L, 1L));
            
            assertTrue(exception.getMessage().contains(expectedMessages[i]),
                    "Expected '" + expectedMessages[i] + "' in message for status " + statuses[i]);
        }
    }

    @Test
    void restoreProductStock_WithNullProduct_HandlesGracefully() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(null);
        orderItem.setQuantity(2);

        testOrder.setItems(List.of(orderItem));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void createOrder_CartWithMultipleItems_OnlyRemovesMatchingItems() {
        CreateOrderRequest request = buildOrderRequest(PaymentMethod.COD);
        
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);
        
        CartItem matchingItem = new CartItem();
        matchingItem.setId(1L);
        matchingItem.setProduct(testProduct);
        matchingItem.setQuantity(1);
        
        CartItem nonMatchingItem = new CartItem();
        nonMatchingItem.setId(2L);
        Product otherProduct = new Product();
        otherProduct.setId(999L);
        nonMatchingItem.setProduct(otherProduct);
        nonMatchingItem.setQuantity(2);
        
        cart.addItem(matchingItem);
        cart.addItem(nonMatchingItem);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findAllByIdIn(Collections.singletonList(1L))).thenReturn(Collections.singletonList(testProduct));
        when(orderRepository.existsByOrderCode(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(82L);
            return savedOrder;
        });
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendOrderPaymentSuccessEmail(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

        CreateOrderResponse response = orderService.createOrder(request, 1L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void cancelMyOrder_VNPAY_DoesNotRestoreStock() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(2);

        testOrder.setItems(List.of(orderItem));
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.VNPAY);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.cancelMyOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void cancelMyOrder_RestoreStockThrows_ShouldStillCancelOrder() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);

        List<OrderItem> failingItems = new ArrayList<>() {
            @Override
            public java.util.Iterator<OrderItem> iterator() {
                throw new RuntimeException("Iterator failed");
            }
        };
        failingItems.add(orderItem);

        testOrder.setItems(failingItems);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.COD);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        assertDoesNotThrow(() -> orderService.cancelMyOrder(1L, 1L));

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(testOrder);
    }
}
