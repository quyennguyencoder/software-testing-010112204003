package com.utephonehub.backend.entity;

import com.utephonehub.backend.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate; // Diagram: effectiveDate: date

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate; // Diagram: expirationDate: date

    @Column(name = "title", nullable = false)
    private String title; // Diagram: title: string

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Diagram: description: string

    @Column(name = "percent_discount")
    private Double percentDiscount; // Diagram: percentDiscount: double

    @Column(name = "fixed_amount")
    private Double fixedAmount; // Fixed discount amount

    @Column(name = "max_discount")
    private Double maxDiscount; // Maximum discount cap for percentage-based promotions

    @Column(name = "min_value_to_be_applied")
    private Double minValueToBeApplied; // Diagram: minValueToBeApplied: double

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EPromotionStatus status; // Diagram: status: EPromotionStatus

    // Relationship: template (1) <-> (1..*) Promotion
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", nullable = false)
    @ToString.Exclude
    private PromotionTemplate template; // Diagram: template: PromotionTemplate

    // Relationship: targets (PromotionTarget[])
    // Quan hệ Aggregation (Hình thoi) trong Diagram
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<PromotionTarget> targets = new ArrayList<>(); // Diagram: targets: PromotionTarget[]
}
