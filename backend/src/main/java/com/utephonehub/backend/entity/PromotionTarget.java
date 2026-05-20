package com.utephonehub.backend.entity;

import com.utephonehub.backend.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotion_targets")
public class PromotionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // Diagram: id: long

    @Column(name = "applicable_object_id", nullable = false)
    private Long applicableObjectId; // Diagram: applicableObjectId: long (Lưu ID của Product/Category...)

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EPromotionTargetType type; // Diagram: type: EPromotionTargetType

    // Relationship: Để JPA hiểu quan hệ Aggregation (Hình thoi rỗng) từ Promotion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
}