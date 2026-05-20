package com.phonehub.backend.service;

import com.phonehub.backend.dto.response.payment.PaymentHistoryResponse;

public interface IPaymentService {
    
    /**
     * Get payment history for customer
     */
    PaymentHistoryResponse getCustomerPaymentHistory(Long userId, int page, int size);
}
