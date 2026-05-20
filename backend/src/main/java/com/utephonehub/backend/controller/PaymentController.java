package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.utephonehub.backend.dto.response.payment.PaymentHistoryResponse;
import com.utephonehub.backend.dto.response.payment.PaymentResponse;
import com.utephonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.utephonehub.backend.service.IPaymentService;
import com.utephonehub.backend.service.IVNPayService;
import com.utephonehub.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment Management APIs")
public class PaymentController {
    
    private final IPaymentService paymentService;
    private final IVNPayService vnPayService;
    private final SecurityUtils securityUtils;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;
    
    /**
     * Create VNPay payment URL
     */
    @PostMapping("/vnpay/create")
    @Operation(summary = "Create VNPay payment URL", description = "Generate VNPay payment URL for order payment")
    public ResponseEntity<ApiResponse<VNPayPaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest servletRequest) {
        
        log.info("Creating VNPay payment for order: {}", request.getOrderId());
        String ipAddress = securityUtils.getClientIp(servletRequest);
        VNPayPaymentResponse response = vnPayService.createPaymentUrl(request, ipAddress);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Payment URL created successfully", response));
    }
    
    /**
     * VNPay payment callback (IPN - Instant Payment Notification)
     * This is called by VNPay server
     */
    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback", description = "Handle payment callback from VNPay")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(HttpServletRequest request) {
        log.info("Received VNPay callback");
        
        PaymentResponse response = vnPayService.handleCallback(request);
        
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }
    
    /**
     * VNPay payment return URL (redirect to frontend)
     * This is where user is redirected after payment
     * NOTE: Only displays result, actual payment processing happens in callback
     */
    @GetMapping("/vnpay/return")
    @Operation(summary = "VNPay payment return", description = "Redirect user after payment")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Received VNPay return");
        
        try {
            // FOR DEVELOPMENT: Simulate callback processing since VNPay can't reach localhost
            // In production, VNPay will call the callback URL directly
            if (activeProfile != null && (activeProfile.contains("dev") || activeProfile.contains("local"))) {
                try {
                    log.info("Simulating VNPay callback for development environment");
                    vnPayService.handleCallback(request);
                    log.info("Callback simulation completed successfully");
                } catch (Exception callbackError) {
                    log.error("Error simulating callback (order may still be processed): {}", callbackError.getMessage());
                    // Continue even if callback fails, let frontend handle the display
                    // The actual payment will be processed by VNPay's IPN in production
                }
            }
            
            // Get all VNPay params to forward to frontend
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_Amount = request.getParameter("vnp_Amount");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_BankCode = request.getParameter("vnp_BankCode");
            
            // Build frontend URL with all params using configured URL
            String paymentReturnUrl = frontendUrl + "/payment/vnpay-return";
            StringBuilder redirectUrl = new StringBuilder(paymentReturnUrl);
            redirectUrl.append("?vnp_TxnRef=").append(vnp_TxnRef != null ? vnp_TxnRef : "");
            redirectUrl.append("&vnp_ResponseCode=").append(vnp_ResponseCode != null ? vnp_ResponseCode : "");
            redirectUrl.append("&vnp_Amount=").append(vnp_Amount != null ? vnp_Amount : "0");
            redirectUrl.append("&vnp_TransactionNo=").append(vnp_TransactionNo != null ? vnp_TransactionNo : "");
            redirectUrl.append("&vnp_BankCode=").append(vnp_BankCode != null ? vnp_BankCode : "");
            
            log.info("Redirecting to frontend: {}", redirectUrl.toString());
            response.sendRedirect(redirectUrl.toString());
            
        } catch (Exception e) {
            log.error("Error processing payment return", e);
            // Fallback: redirect to frontend with error using configured URL
            response.sendRedirect(frontendUrl + "/payment/vnpay-return?vnp_ResponseCode=99");
        }
    }
    
    /**
     * Get customer payment history
     */
    @GetMapping("/history")
    @Operation(summary = "Get customer payment history", description = "Get payment history for logged in customer with pagination")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PaymentHistoryResponse>> getPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        Long userId = securityUtils.getCurrentUserId(request);
        log.info("Getting payment history for user: {}, page: {}, size: {}", userId, page, size);
        
        PaymentHistoryResponse response = paymentService.getCustomerPaymentHistory(userId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", response));
    }
}
