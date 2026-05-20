package com.utephonehub.backend.entity;

import com.utephonehub.backend.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "promotion_templates")
public class PromotionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "code", unique = true, nullable = false)
    private String code; // Diagram: code: string

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Diagram: createdAt: date

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EPromotionTemplateType type; // Diagram: type: EPromotionTemplateType

    // Relationship (Không ghi trong ô attribute nhưng thể hiện qua đường nối 1..*)
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Promotion> promotions;
}