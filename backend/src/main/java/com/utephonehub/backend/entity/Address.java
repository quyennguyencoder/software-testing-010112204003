package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String streetAddress;

    @Column(length = 100)
    private String ward;

    @Column(length = 20)
    private String wardCode;

    @Column(length = 100)
    private String province;

    @Column(length = 20)
    private String provinceCode;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

