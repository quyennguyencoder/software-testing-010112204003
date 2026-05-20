package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.PaymentCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentCallbackLogRepository extends JpaRepository<PaymentCallbackLog, Long> {
    
    /**
     * Tìm tất cả callback logs của một payment
     */
    List<PaymentCallbackLog> findByPaymentIdOrderByReceivedAtDesc(Long paymentId);
    
    /**
     * Kiểm tra xem có callback nào với signature này chưa (tránh duplicate)
     */
    boolean existsByPaymentIdAndSignature(Long paymentId, String signature);
}
