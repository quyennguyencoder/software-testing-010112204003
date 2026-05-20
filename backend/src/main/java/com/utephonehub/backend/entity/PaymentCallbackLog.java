package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_callback_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;  // JSON string của toàn bộ params từ VNPay
    
    @Column(name = "response_code")
    private String responseCode;  // vnp_ResponseCode
    
    @Column(name = "transaction_id")
    private String transactionId;  // vnp_TransactionNo
    
    @Column(name = "signature")
    private String signature;  // vnp_SecureHash
    
    @Column(name = "signature_valid")
    private Boolean signatureValid;  // true nếu signature hợp lệ
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;  // Lỗi nếu có
    
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
    
    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
}
