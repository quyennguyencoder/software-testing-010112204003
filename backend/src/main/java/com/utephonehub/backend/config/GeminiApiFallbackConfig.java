package com.utephonehub.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình cho Gemini API với fallback keys
 * Tránh rate limit bằng cách xoay vòng giữa nhiều API keys
 */
@Configuration
@Getter
public class GeminiApiFallbackConfig {
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;
    
    @Value("${gemini.model:gemma-3-12b-it}")
    private String model;
    
    @Value("${gemini.embedding.model:text-embedding-004}")
    private String embeddingModel;
    
    // 10 API Keys
    @Value("${gemini.api.key.1:}")
    private String apiKey1;
    
    @Value("${gemini.api.key.2:}")
    private String apiKey2;
    
    @Value("${gemini.api.key.3:}")
    private String apiKey3;
    
    @Value("${gemini.api.key.4:}")
    private String apiKey4;
    
    @Value("${gemini.api.key.5:}")
    private String apiKey5;
    
    @Value("${gemini.api.key.6:}")
    private String apiKey6;
    
    @Value("${gemini.api.key.7:}")
    private String apiKey7;
    
    @Value("${gemini.api.key.8:}")
    private String apiKey8;
    
    @Value("${gemini.api.key.9:}")
    private String apiKey9;
    
    @Value("${gemini.api.key.10:}")
    private String apiKey10;
    
    // Fallback Settings
    @Value("${gemini.fallback.enabled:true}")
    private Boolean fallbackEnabled;
    
    @Value("${gemini.fallback.retry-count:3}")
    private Integer retryCount;
    
    @Value("${gemini.fallback.timeout-ms:5000}")
    private Long timeoutMs;
    
    /**
     * Lấy danh sách tất cả API keys (loại bỏ empty values)
     */
    public List<String> getAllApiKeys() {
        List<String> keys = new ArrayList<>();
        
        // Add non-empty keys theo thứ tự
        if (apiKey1 != null && !apiKey1.isEmpty() && !apiKey1.startsWith("YOUR_")) 
            keys.add(apiKey1);
        if (apiKey2 != null && !apiKey2.isEmpty() && !apiKey2.startsWith("YOUR_")) 
            keys.add(apiKey2);
        if (apiKey3 != null && !apiKey3.isEmpty() && !apiKey3.startsWith("YOUR_")) 
            keys.add(apiKey3);
        if (apiKey4 != null && !apiKey4.isEmpty() && !apiKey4.startsWith("YOUR_")) 
            keys.add(apiKey4);
        if (apiKey5 != null && !apiKey5.isEmpty() && !apiKey5.startsWith("YOUR_")) 
            keys.add(apiKey5);
        if (apiKey6 != null && !apiKey6.isEmpty() && !apiKey6.startsWith("YOUR_")) 
            keys.add(apiKey6);
        if (apiKey7 != null && !apiKey7.isEmpty() && !apiKey7.startsWith("YOUR_")) 
            keys.add(apiKey7);
        if (apiKey8 != null && !apiKey8.isEmpty() && !apiKey8.startsWith("YOUR_")) 
            keys.add(apiKey8);
        if (apiKey9 != null && !apiKey9.isEmpty() && !apiKey9.startsWith("YOUR_")) 
            keys.add(apiKey9);
        if (apiKey10 != null && !apiKey10.isEmpty() && !apiKey10.startsWith("YOUR_")) 
            keys.add(apiKey10);
        
        return keys;
    }
    
    /**
     * Lấy endpoint cho text generation
     */
    public String getGenerateEndpoint(String apiKey) {
        return String.format("%s/%s:generateContent?key=%s", baseUrl, model, apiKey);
    }
    
    /**
     * Lấy endpoint cho embedding
     */
    public String getEmbeddingEndpoint(String apiKey) {
        return String.format("%s/%s:batchEmbedContent?key=%s", baseUrl, embeddingModel, apiKey);
    }
}
