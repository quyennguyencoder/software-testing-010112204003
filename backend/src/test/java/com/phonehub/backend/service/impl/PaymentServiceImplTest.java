package com.phonehub.backend.service.impl;

import com.phonehub.backend.dto.response.payment.PaymentHistoryResponse;
import com.phonehub.backend.dto.response.payment.PaymentResponse;
import com.phonehub.backend.entity.Payment;
import com.phonehub.backend.mapper.PaymentMapper;
import com.phonehub.backend.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getCustomerPaymentHistory_Success() {
        Payment testPayment = new Payment();
        testPayment.setId(1L);

        Page<Payment> page = new PageImpl<>(Collections.singletonList(testPayment), PageRequest.of(0, 10), 1);
        when(paymentRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        
        PaymentResponse response = new PaymentResponse();
        when(paymentMapper.toPaymentResponse(testPayment)).thenReturn(response);

        PaymentHistoryResponse result = paymentService.getCustomerPaymentHistory(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getPayments().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }
}
