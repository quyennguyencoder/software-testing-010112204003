package com.utephonehub.backend.repository;

import com.utephonehub.backend.entity.ChatbotConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho ChatbotConfig
 */
@Repository
public interface ChatbotConfigRepository extends JpaRepository<ChatbotConfig, Long> {
    
    /**
     * Tìm cấu hình theo key
     */
    Optional<ChatbotConfig> findByConfigKey(String configKey);
    
    /**
     * Kiểm tra key đã tồn tại chưa
     */
    boolean existsByConfigKey(String configKey);
}
