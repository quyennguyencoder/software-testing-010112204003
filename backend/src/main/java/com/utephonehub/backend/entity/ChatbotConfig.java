package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ cấu hình chatbot
 * Cho phép Admin bật/tắt tính năng chatbot để quản lý chi phí và phòng tấn công
 */
@Entity
@Table(name = "chatbot_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Khóa cấu hình (config key)
     * VD: "CHATBOT_ENABLED", "MAX_REQUESTS_PER_MINUTE"
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 50)
    private String configKey;

    /**
     * Giá trị cấu hình
     * VD: "true", "false", "100"
     */
    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;

    /**
     * Mô tả cấu hình
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Admin đã cập nhật lần cuối
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== STATIC KEYS ====================
    
    public static final String KEY_CHATBOT_ENABLED = "CHATBOT_ENABLED";
    public static final String KEY_FALLBACK_MODE = "FALLBACK_MODE";
    public static final String KEY_MAX_REQUESTS_PER_MINUTE = "MAX_REQUESTS_PER_MINUTE";
}
