package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.payment.PaymentHistoryResponse;
import com.utephonehub.backend.dto.response.payment.PaymentResponse;
import com.utephonehub.backend.entity.Payment;
import com.utephonehub.backend.mapper.PaymentMapper;
import com.utephonehub.backend.repository.PaymentRepository;
import com.utephonehub.backend.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentHistoryResponse getCustomerPaymentHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> paymentPage = paymentRepository.findByUserId(userId, pageable);
        
        List<PaymentResponse> payments = paymentPage.getContent().stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
        
        return PaymentHistoryResponse.builder()
                .payments(payments)
                .currentPage(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .hasNext(paymentPage.hasNext())
                .hasPrevious(paymentPage.hasPrevious())
                .build();
    }
}
