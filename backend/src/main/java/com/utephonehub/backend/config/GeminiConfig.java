package com.utephonehub.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình cho Gemini API
 */
@Configuration
@Getter
public class GeminiConfig {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;
    
    @Value("${gemini.model:gemma-3-12b-it}")
    private String model;
    
    @Value("${gemini.embedding.model:text-embedding-004}")
    private String embeddingModel;
    
    public String getGenerateEndpoint() {
        return String.format("%s/%s:generateContent?key=%s", baseUrl, model, apiKey);
    }
    
    public String getEmbeddingEndpoint() {
        return String.format("%s/%s:batchEmbedContent?key=%s", baseUrl, embeddingModel, apiKey);
    }
}
