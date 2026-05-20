package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {

    Optional<Promotion> findByTemplateCode(String code);

    List<Promotion> findByEffectiveDateBeforeAndExpirationDateAfter(LocalDateTime now1, LocalDateTime now2);
}