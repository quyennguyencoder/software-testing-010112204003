package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.response.payment.PaymentHistoryResponse;

public interface IPaymentService {
    
    /**
     * Get payment history for customer
     */
    PaymentHistoryResponse getCustomerPaymentHistory(Long userId, int page, int size);
}
