package com.phonehub.backend.service;

import com.phonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.phonehub.backend.dto.response.payment.PaymentResponse;
import com.phonehub.backend.dto.response.payment.VNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IVNPayService {
    
    /**
     * Create VNPay payment URL
     */
    VNPayPaymentResponse createPaymentUrl(CreatePaymentRequest request, String ipAddress);
    
    /**
     * Handle VNPay payment callback/return
     */
    PaymentResponse handleCallback(HttpServletRequest request);
    
    /**
     * Get payment status without processing (for return URL)
     */
    PaymentResponse getPaymentStatus(String orderCode);
}
