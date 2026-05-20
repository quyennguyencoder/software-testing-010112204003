package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.PromotionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionTemplateRepository extends JpaRepository<PromotionTemplate, String> {
    
    /**
     * Check if a template with the given code already exists
     */
    boolean existsByCode(String code);
    
    /**
     * Check if a template with the given code exists, excluding a specific template ID
     * Used for update validation to allow keeping the same code
     */
    boolean existsByCodeAndIdNot(String code, String id);
    
    /**
     * Check if any promotion is using this template
     * More efficient than loading all promotions
     */
    @Query("SELECT COUNT(p) > 0 FROM Promotion p WHERE p.template.id = :templateId")
    boolean isTemplateInUse(@Param("templateId") String templateId);
}