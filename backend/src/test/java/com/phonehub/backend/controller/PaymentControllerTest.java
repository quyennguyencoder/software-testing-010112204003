package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.phonehub.backend.dto.response.payment.PaymentHistoryResponse;
import com.phonehub.backend.dto.response.payment.PaymentResponse;
import com.phonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.phonehub.backend.service.intf.IPaymentService;
import com.phonehub.backend.service.intf.IVNPayService;
import com.phonehub.backend.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPaymentService paymentService;

    @Mock
    private IVNPayService vnPayService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        ReflectionTestUtils.setField(paymentController, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(paymentController, "activeProfile", "dev");
    }

    @Test
    public void createPayment_ShouldReturnPaymentUrl() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(10L);
        request.setAmount(10000L);

        VNPayPaymentResponse response = VNPayPaymentResponse.builder()
                .paymentUrl("http://vnpay.com/pay")
                .build();

        when(securityUtils.getClientIp(any())).thenReturn("127.0.0.1");
        when(vnPayService.createPaymentUrl(any(CreatePaymentRequest.class), eq("127.0.0.1"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/vnpay/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentUrl").value("http://vnpay.com/pay"));
    }

    @Test
    public void paymentCallback_ShouldReturnPaymentResponse() throws Exception {
        PaymentResponse response = new PaymentResponse();

        when(vnPayService.handleCallback(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/vnpay/callback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void paymentReturn_ShouldRedirectToFrontend() throws Exception {
        when(vnPayService.handleCallback(any())).thenReturn(new PaymentResponse());

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN123")
                .param("vnp_ResponseCode", "00")
                .param("vnp_Amount", "100000")
                .param("vnp_TransactionNo", "TR123")
                .param("vnp_BankCode", "NCB"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void getPaymentHistory_ShouldReturnHistory() throws Exception {
        Long userId = 1L;
        PaymentHistoryResponse response = new PaymentHistoryResponse();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(paymentService.getCustomerPaymentHistory(userId, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    // ============ ADDITIONAL TESTS FOR FULL COVERAGE ============
    
    @Test
    public void getPaymentHistory_WithCustomPagination_ShouldReturnHistory() throws Exception {
        Long userId = 1L;
        PaymentHistoryResponse response = new PaymentHistoryResponse();

        when(securityUtils.getCurrentUserId(any())).thenReturn(userId);
        when(paymentService.getCustomerPaymentHistory(userId, 1, 20)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/history?page=1&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void paymentReturn_WithDevProfile_ShouldSimulateCallback() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", "dev");
        when(vnPayService.handleCallback(any())).thenReturn(new PaymentResponse());

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN123")
                .param("vnp_ResponseCode", "00")
                .param("vnp_Amount", "100000")
                .param("vnp_TransactionNo", "TR123")
                .param("vnp_BankCode", "NCB"))
                .andExpect(status().is3xxRedirection());

        verify(vnPayService, times(1)).handleCallback(any());
    }

    @Test
    public void paymentReturn_WithLocalProfile_ShouldSimulateCallback() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", "local");
        when(vnPayService.handleCallback(any())).thenReturn(new PaymentResponse());

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN456")
                .param("vnp_ResponseCode", "00")
                .param("vnp_Amount", "200000"))
                .andExpect(status().is3xxRedirection());

        verify(vnPayService, times(1)).handleCallback(any());
    }

    @Test
    public void paymentReturn_WithProdProfile_ShouldNotSimulateCallback() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", "prod");

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN789")
                .param("vnp_ResponseCode", "00"))
                .andExpect(status().is3xxRedirection());

        verify(vnPayService, never()).handleCallback(any());
    }

    @Test
    public void paymentReturn_WithNullProfile_ShouldNotSimulateCallback() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", null);

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN999"))
                .andExpect(status().is3xxRedirection());

        verify(vnPayService, never()).handleCallback(any());
    }

    @Test
    public void paymentReturn_WithDevProfileAndCallbackException_ShouldContinue() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", "dev");
        when(vnPayService.handleCallback(any())).thenThrow(new RuntimeException("Callback error"));

        mockMvc.perform(get("/api/v1/payments/vnpay/return")
                .param("vnp_TxnRef", "TXN111")
                .param("vnp_ResponseCode", "00"))
                .andExpect(status().is3xxRedirection());

        verify(vnPayService, times(1)).handleCallback(any());
    }

    @Test
    public void paymentReturn_WithNullParameters_ShouldHandleGracefully() throws Exception {
        ReflectionTestUtils.setField(paymentController, "activeProfile", "prod");

        mockMvc.perform(get("/api/v1/payments/vnpay/return"))
                .andExpect(status().is3xxRedirection());
    }
}
