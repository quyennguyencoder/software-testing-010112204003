package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.PromotionTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionTargetRepository extends JpaRepository<PromotionTarget, Long> {
    
    /**
     * Tìm tất cả sản phẩm đang có khuyến mãi ACTIVE
     */
    @Query("SELECT DISTINCT pt.applicableObjectId " +
           "FROM PromotionTarget pt " +
           "JOIN pt.promotion p " +
           "WHERE pt.type = 'PRODUCT' " +
           "AND p.status = 'ACTIVE' " +
           "AND p.effectiveDate <= CURRENT_TIMESTAMP " +
           "AND p.expirationDate >= CURRENT_TIMESTAMP")
    List<Long> findActivePromotionProductIds();
}
