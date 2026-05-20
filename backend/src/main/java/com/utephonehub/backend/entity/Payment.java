package com.utephonehub.backend.entity;

import com.utephonehub.backend.enums.EWalletProvider;
import com.utephonehub.backend.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private EWalletProvider provider;

    @Column(length = 100)
    private String transactionId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;  // Ghi chú của admin (đối soát, note...)

    @Column(nullable = true)  // Allow NULL for existing records
    @Builder.Default
    private Boolean reconciled = false;  // Đã đối soát chưa

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
