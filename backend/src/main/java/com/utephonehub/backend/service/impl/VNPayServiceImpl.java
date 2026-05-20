package com.utephonehub.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utephonehub.backend.config.VNPayConfig;
import com.utephonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.utephonehub.backend.dto.response.payment.PaymentResponse;
import com.utephonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.Payment;
import com.utephonehub.backend.entity.PaymentCallbackLog;
import com.utephonehub.backend.enums.EWalletProvider;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.mapper.PaymentMapper;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.PaymentCallbackLogRepository;
import com.utephonehub.backend.repository.PaymentRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.service.IEmailService;
import com.utephonehub.backend.service.IVNPayService;
import com.utephonehub.backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements IVNPayService {
    
    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final ProductRepository productRepository;
    private final PaymentMapper paymentMapper;
    private final IEmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public VNPayPaymentResponse createPaymentUrl(CreatePaymentRequest request, String ipAddress) {
        log.info("Creating VNPay payment for order: {}", request.getOrderId());
        
        // 1. Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
        
        // 2. Validate order status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status");
        }
        
        // 3. Validate amount matches order total
        long amountInVND = request.getAmount();
        long expectedAmount = order.getTotalAmount().longValue();
        if (amountInVND != expectedAmount) {
            throw new BadRequestException("Payment amount does not match order total");
        }
        
        // 3.1. Check if PENDING payment already exists for this order (prevent duplicates)
        Payment existingPayment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.PENDING) {
            log.info("PENDING payment already exists for order: {}. Skipping duplicate creation.", order.getOrderCode());
            // Note: We still generate a new URL because the old one may have expired
        }
        
        try {
            // 4. Build VNPay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amountInVND * 100)); // VNPay requires amount * 100
            vnpParams.put("vnp_CurrCode", "VND");
            
            // Use order code as transaction reference
            vnpParams.put("vnp_TxnRef", order.getOrderCode());
            
            String orderInfo = request.getOrderInfo() != null 
                ? request.getOrderInfo() 
                : "Thanh toan don hang " + order.getOrderCode();
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            
            String locale = request.getLocale() != null ? request.getLocale() : "vn";
            vnpParams.put("vnp_Locale", locale);
            
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);
            
            // 5. Set create date and expire date (Vietnam time GMT+7)
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String vnpCreateDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            calendar.add(Calendar.MINUTE, 15); // Payment expires in 15 minutes
            String vnpExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);
            
            // 6. Build query URL
            String queryUrl = VNPayUtil.buildQuery(vnpParams);
            
            // 7. Build hash data for signature
            String hashData = VNPayUtil.buildHashData(vnpParams);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            
            // Debug logging
            log.info("=== VNPay Debug Info ===");
            log.info("Hash Secret: {}", vnPayConfig.getHashSecret());
            log.info("Hash Data: {}", hashData);
            log.info("Secure Hash: {}", vnpSecureHash);
            log.info("========================");
            
            // 8. Create or reuse Payment record with PENDING status
            Payment payment;
            if (existingPayment != null && existingPayment.getStatus() == PaymentStatus.PENDING) {
                // Reuse existing PENDING payment (don't create duplicate)
                payment = existingPayment;
                log.info("Reusing existing PENDING payment for order: {}", order.getOrderCode());
            } else {
                // Create new Payment record
                payment = Payment.builder()
                        .order(order)
                        .provider(EWalletProvider.VNPAY)
                        .transactionId(null)  // Will be updated in callback
                        .amount(order.getTotalAmount())
                        .status(PaymentStatus.PENDING)
                        .note("Waiting for VNPay payment confirmation")
                        .reconciled(false)
                        .build();
                paymentRepository.save(payment);
                log.info("Created Payment record with PENDING status for order: {}", order.getOrderCode());
            }
            
            // 9. Final payment URL
            String paymentUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;
            
            log.info("VNPay payment URL created successfully for order: {}", order.getOrderCode());
            
            return VNPayPaymentResponse.builder()
                    .code("00")
                    .message("Success")
                    .paymentUrl(paymentUrl)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            throw new RuntimeException("Error creating VNPay payment: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public PaymentResponse handleCallback(HttpServletRequest request) {
        log.info("Handling VNPay payment callback");
        
        // 1. Get all parameters from VNPay
        Map<String, String> fields = new HashMap<>();
        for (String param : request.getParameterMap().keySet()) {
            String value = request.getParameter(param);
            if (value != null && !value.isEmpty()) {
                fields.put(param, value);
            }
        }
        
        // 2. Get secure hash from VNPay
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        
        // 3. Remove hash fields before verification
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        // 4. Verify signature
        String hashData = VNPayUtil.buildHashData(fields);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        boolean signatureValid = calculatedHash.equals(vnpSecureHash);
        
        // 5. Get transaction info
        String vnpTxnRef = request.getParameter("vnp_TxnRef"); // This is orderCode
        String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpTransactionStatus = request.getParameter("vnp_TransactionStatus");
        long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount")) / 100; // Convert back from VNPay format
        
        // 6. Find order by order code
        Order order = orderRepository.findByOrderCode(vnpTxnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + vnpTxnRef));
        
        // 7. Create or update payment record
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElse(Payment.builder()
                        .order(order)
                        .provider(EWalletProvider.VNPAY)
                        .amount(BigDecimal.valueOf(vnpAmount))
                        .status(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build());
        
        payment.setTransactionId(vnpTransactionNo);
        
        // 7.1. Nếu signature không hợp lệ, throw exception NGAY
        if (!signatureValid) {
            log.error("Invalid VNPay signature");
            throw new BadRequestException("Invalid payment signature");
        }
        
        // 8. CHỈ XỬ LÝ NÊU ĐƠN HÀNG CHƯA ĐƯỢC CONFIRMED (Tránh trừ tồn kho 2 lần)
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.info("Order {} already confirmed. Skipping payment processing to avoid duplicate stock deduction.", order.getOrderCode());
            // Chỉ cập nhật payment record nếu chưa có transaction ID
            if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
                payment.setTransactionId(vnpTransactionNo);
                paymentRepository.save(payment);
            }
            return paymentMapper.toPaymentResponse(payment);
        }
        
        // 8.1. Update payment and order status based on VNPay response
        if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
            // Payment successful
            payment.setStatus(PaymentStatus.SUCCESS);
            
            // 8.2. Validate stock availability before confirming
            // Stock is at ProductTemplate level, calculate total available stock per product
            boolean allStockAvailable = true;
            for (var orderItem : order.getItems()) {
                var product = orderItem.getProduct();
                int totalAvailableStock = product.getTemplates().stream()
                        .filter(t -> t.getStatus() != null && t.getStatus())
                        .mapToInt(t -> t.getStockQuantity() != null ? t.getStockQuantity() : 0)
                        .sum();
                if (totalAvailableStock < orderItem.getQuantity()) {
                    allStockAvailable = false;
                    break;
                }
            }
            
            if (allStockAvailable) {
                order.setStatus(OrderStatus.CONFIRMED);
                // Reduce stock from templates sequentially (same pattern as OrderServiceImpl)
                for (var orderItem : order.getItems()) {
                    var product = orderItem.getProduct();
                    int remainingQuantity = orderItem.getQuantity();
                    
                    // Deduct stock from available templates sequentially
                    for (var template : product.getTemplates()) {
                        if (template.getStatus() == null || !template.getStatus() 
                                || template.getStockQuantity() == null || template.getStockQuantity() <= 0 
                                || remainingQuantity <= 0) {
                            continue;
                        }
                        
                        int deductAmount = Math.min(template.getStockQuantity(), remainingQuantity);
                        int oldStock = template.getStockQuantity();
                        template.setStockQuantity(oldStock - deductAmount);
                        remainingQuantity -= deductAmount;
                        log.info("Reduced stock for product {} template {}: {} -> {}", 
                            product.getId(), template.getSku(), oldStock, template.getStockQuantity());
                    }
                    
                    productRepository.save(product); // Cascade saves templates
                }
                log.info("Payment successful and stock reduced for order: {}", order.getOrderCode());
                
                // Send payment success email (async, không block payment flow)
                try {
                    emailService.sendOrderPaymentSuccessEmail(
                        order.getEmail(),
                        order.getOrderCode(),
                        order.getTotalAmount(),
                        order.getRecipientName(),
                        order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "VNPay"
                    );
                } catch (Exception e) {
                    log.error("Failed to send payment success email for order {}: {}", 
                             order.getOrderCode(), e.getMessage());
                    // Không throw exception
                }
            } else {
                // Handle out of stock scenario: Cancel order but keep payment success (for refund)
                order.setStatus(OrderStatus.CANCELLED);
                payment.setNote("Payment successful but out of stock. Refund required.");
                log.warn("Order {} paid but out of stock. Cancelled for refund.", order.getOrderCode());
            }
        } else {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);
            log.warn("Payment failed for order: {} with response code: {}", order.getOrderCode(), vnpResponseCode);
        }
        
        paymentRepository.save(payment);
        orderRepository.save(order);
        
        // 9. LƯU CALLBACK LOG SAU KHI PAYMENT ĐÃ CÓ ID (Audit trail)
        try {
            PaymentCallbackLog callbackLog = PaymentCallbackLog.builder()
                    .payment(payment)
                    .requestData(objectMapper.writeValueAsString(fields))
                    .responseCode(vnpResponseCode)
                    .transactionId(vnpTransactionNo)
                    .signature(vnpSecureHash)
                    .signatureValid(signatureValid)
                    .errorMessage(null) // Signature đã valid nếu tới đây
                    .build();
            callbackLogRepository.save(callbackLog);
            log.info("Saved callback log for payment ID: {}", payment.getId());
        } catch (Exception e) {
            log.error("Error saving callback log", e);
            // Không throw exception để không ảnh hưởng flow chính
        }
        
        // 10. Return payment response
        return paymentMapper.toPaymentResponse(payment);
    }
    
    /**
     * Get payment status without processing (for return URL)
     * This method only queries existing payment status without triggering any business logic
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String orderCode) {
        log.info("Getting payment status for order: {}", orderCode);
        
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + orderCode));
        
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderCode));
        
        return paymentMapper.toPaymentResponse(payment);
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
