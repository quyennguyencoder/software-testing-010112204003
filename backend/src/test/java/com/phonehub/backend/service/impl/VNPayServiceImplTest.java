package com.phonehub.backend.service.impl;

import com.phonehub.backend.config.VNPayConfig;
import com.phonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.phonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.phonehub.backend.dto.response.payment.PaymentResponse;
import com.phonehub.backend.entity.Order;
import com.phonehub.backend.entity.OrderItem;
import com.phonehub.backend.entity.Payment;
import com.phonehub.backend.entity.Product;
import com.phonehub.backend.entity.ProductTemplate;
import com.phonehub.backend.enums.EWalletProvider;
import com.phonehub.backend.enums.OrderStatus;
import com.phonehub.backend.enums.PaymentStatus;
import com.phonehub.backend.exception.BadRequestException;
import com.phonehub.backend.exception.ResourceNotFoundException;
import com.phonehub.backend.mapper.PaymentMapper;
import com.phonehub.backend.repository.OrderRepository;
import com.phonehub.backend.repository.PaymentCallbackLogRepository;
import com.phonehub.backend.repository.PaymentRepository;
import com.phonehub.backend.repository.ProductRepository;
import com.phonehub.backend.service.intf.IEmailService;
import com.phonehub.backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import java.lang.reflect.Method;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VNPayServiceImplTest {

    @Mock
    private VNPayConfig vnPayConfig;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentCallbackLogRepository callbackLogRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private IEmailService emailService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private VNPayServiceImpl vnPayService;

    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderCode("ORD123");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(100000));
        testOrder.setItems(new ArrayList<>());
        
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrder(testOrder);
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setProvider(EWalletProvider.VNPAY);
    }

    @Test
    void createPaymentUrl_Success() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(1L);
        req.setAmount(100000L);
        req.setOrderInfo("Test Order");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(vnPayConfig.getVersion()).thenReturn("2.1.0");
        when(vnPayConfig.getCommand()).thenReturn("pay");
        when(vnPayConfig.getTmnCode()).thenReturn("TMNCODE1");
        when(vnPayConfig.getOrderType()).thenReturn("other");
        when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/return");
        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.getVnpayUrl()).thenReturn("http://vnpay.com/pay");
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildQuery(any())).thenReturn("query=test");
            utilities.when(() -> VNPayUtil.buildHashData(any())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            VNPayPaymentResponse response = vnPayService.createPaymentUrl(req, "127.0.0.1");

            assertNotNull(response);
            assertEquals("00", response.getCode());
            assertEquals("Success", response.getMessage());
            assertTrue(response.getPaymentUrl().contains("vnp_SecureHash=secureHash"));
            
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void createPaymentUrl_ReusesExistingPendingPayment() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(1L);
        req.setAmount(100000L);
        req.setOrderInfo("Test Order");

        Payment pendingPayment = new Payment();
        pendingPayment.setStatus(PaymentStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(pendingPayment));
        when(vnPayConfig.getVersion()).thenReturn("2.1.0");
        when(vnPayConfig.getCommand()).thenReturn("pay");
        when(vnPayConfig.getTmnCode()).thenReturn("TMNCODE1");
        when(vnPayConfig.getOrderType()).thenReturn("other");
        when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/return");
        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(vnPayConfig.getVnpayUrl()).thenReturn("http://vnpay.com/pay");

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildQuery(any())).thenReturn("query=test");
            utilities.when(() -> VNPayUtil.buildHashData(any())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            VNPayPaymentResponse response = vnPayService.createPaymentUrl(req, "127.0.0.1");

            assertNotNull(response);
            assertEquals("00", response.getCode());
            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Test
    void createPaymentUrl_OrderNotFound_ThrowsException() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(99L);
        
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> vnPayService.createPaymentUrl(req, "127.0.0.1"));
    }

    @Test
    void createPaymentUrl_InvalidStatus_ThrowsException() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(1L);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        assertThrows(BadRequestException.class, () -> vnPayService.createPaymentUrl(req, "127.0.0.1"));
    }

    @Test
    void createPaymentUrl_AmountMismatch_ThrowsException() {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(1L);
        req.setAmount(200000L); // Mismatch amount
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        assertThrows(BadRequestException.class, () -> vnPayService.createPaymentUrl(req, "127.0.0.1"));
    }

    @Test
    void handleCallback_InvalidSignature_ThrowsBadRequestException() {
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.addParameter("vnp_SecureHash", "bad-signature");
        callbackRequest.addParameter("vnp_TxnRef", "ORD123");
        callbackRequest.addParameter("vnp_Amount", "10000000");

        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");

        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(testOrder));

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildHashData(anyMap())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("calculated-hash");

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> vnPayService.handleCallback(callbackRequest));

            assertEquals("Invalid payment signature", exception.getMessage());
        }
    }

    @Test
    void handleCallback_OutOfStock_CancelsOrder() {
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.addParameter("vnp_SecureHash", "secureHash");
        callbackRequest.addParameter("vnp_TxnRef", "ORD123");
        callbackRequest.addParameter("vnp_TransactionNo", "TXN-001");
        callbackRequest.addParameter("vnp_ResponseCode", "00");
        callbackRequest.addParameter("vnp_TransactionStatus", "00");
        callbackRequest.addParameter("vnp_Amount", "10000000");

        ProductTemplate template = new ProductTemplate();
        template.setStatus(true);
        template.setStockQuantity(0);

        Product product = new Product();
        product.setId(1L);
        product.setTemplates(Collections.singletonList(template));

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);

        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(Collections.singletonList(orderItem));

        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildHashData(anyMap())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            PaymentResponse response = vnPayService.handleCallback(callbackRequest);

            assertNotNull(response);
            assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    @Test
    void handleCallback_SuccessConfirmsOrderAndReducesStock() {
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.addParameter("vnp_SecureHash", "secureHash");
        callbackRequest.addParameter("vnp_TxnRef", "ORD123");
        callbackRequest.addParameter("vnp_TransactionNo", "TXN-002");
        callbackRequest.addParameter("vnp_ResponseCode", "00");
        callbackRequest.addParameter("vnp_TransactionStatus", "00");
        callbackRequest.addParameter("vnp_Amount", "10000000");

        ProductTemplate template = new ProductTemplate();
        template.setStatus(true);
        template.setStockQuantity(5);
        template.setSku("SKU-001");

        Product product = new Product();
        product.setId(1L);
        product.setTemplates(Collections.singletonList(template));

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);

        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setEmail("test@example.com");
        testOrder.setRecipientName("Test User");
        testOrder.setItems(Collections.singletonList(orderItem));

        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildHashData(anyMap())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            PaymentResponse response = vnPayService.handleCallback(callbackRequest);

            assertNotNull(response);
            assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verify(orderRepository, times(1)).save(testOrder);
            verify(productRepository, times(1)).save(product);
            verify(callbackLogRepository, times(1)).save(any());
        }
    }

    @Test
    void handleCallback_AlreadyConfirmedOrder_UpdatesPaymentOnly() {
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.addParameter("vnp_SecureHash", "secureHash");
        callbackRequest.addParameter("vnp_TxnRef", "ORD123");
        callbackRequest.addParameter("vnp_TransactionNo", "TXN-003");
        callbackRequest.addParameter("vnp_ResponseCode", "00");
        callbackRequest.addParameter("vnp_TransactionStatus", "00");
        callbackRequest.addParameter("vnp_Amount", "10000000");

        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(testOrder));

        Payment existingPayment = new Payment();
        existingPayment.setId(2L);
        existingPayment.setStatus(PaymentStatus.PENDING);
        existingPayment.setTransactionId(null);

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existingPayment));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildHashData(anyMap())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            PaymentResponse response = vnPayService.handleCallback(callbackRequest);

            assertNotNull(response);
            assertEquals("TXN-003", existingPayment.getTransactionId());
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(callbackLogRepository, never()).save(any());
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Test
    void handleCallback_FailedPayment_CancelsOrder() {
        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.addParameter("vnp_SecureHash", "secureHash");
        callbackRequest.addParameter("vnp_TxnRef", "ORD123");
        callbackRequest.addParameter("vnp_TransactionNo", "TXN-004");
        callbackRequest.addParameter("vnp_ResponseCode", "01");
        callbackRequest.addParameter("vnp_TransactionStatus", "00");
        callbackRequest.addParameter("vnp_Amount", "10000000");

        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setItems(Collections.emptyList());

        when(vnPayConfig.getHashSecret()).thenReturn("SECRET");
        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toPaymentResponse(any(Payment.class))).thenReturn(new PaymentResponse());

        try (MockedStatic<VNPayUtil> utilities = mockStatic(VNPayUtil.class)) {
            utilities.when(() -> VNPayUtil.buildHashData(anyMap())).thenReturn("hashdata");
            utilities.when(() -> VNPayUtil.hmacSHA512(anyString(), anyString())).thenReturn("secureHash");

            PaymentResponse response = vnPayService.handleCallback(callbackRequest);

            assertNotNull(response);
            assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verify(orderRepository, times(1)).save(testOrder);
            verify(callbackLogRepository, times(1)).save(any());
        }
    }

    @Test
    void getPaymentStatus_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderCode("ORD123");

        Payment payment = new Payment();
        payment.setId(1L);

        when(orderRepository.findByOrderCode("ORD123")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(new PaymentResponse());

        PaymentResponse response = vnPayService.getPaymentStatus("ORD123");

        assertNotNull(response);
        verify(paymentMapper, times(1)).toPaymentResponse(payment);
    }

    @Test
    void getIpAddress_UsesXForwardedForHeader() throws Exception {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-FORWARDED-FOR", "8.8.8.8");

        Method method = VNPayServiceImpl.class.getDeclaredMethod("getIpAddress", HttpServletRequest.class);
        method.setAccessible(true);

        String ipAddress = (String) method.invoke(vnPayService, httpRequest);

        assertEquals("8.8.8.8", ipAddress);
    }

    @Test
    void getIpAddress_FallsBackToRemoteAddr() throws Exception {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("127.0.0.1");

        Method method = VNPayServiceImpl.class.getDeclaredMethod("getIpAddress", HttpServletRequest.class);
        method.setAccessible(true);

        String ipAddress = (String) method.invoke(vnPayService, httpRequest);

        assertEquals("127.0.0.1", ipAddress);
    }
}
