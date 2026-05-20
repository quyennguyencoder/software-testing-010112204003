package com.utephonehub.backend.service;

import com.utephonehub.backend.config.GeminiApiFallbackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service quản lý fallback API keys cho Gemini
 * Tự động xoay vòng giữa các keys khi một key hết quota
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IGeminiFallbackService {
    
    private final GeminiApiFallbackConfig config;
    private final RestTemplate restTemplate;
    
    // Current key index for round-robin
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);
    
    // Track failed keys to avoid using them repeatedly
    private static final int MAX_CONSECUTIVE_FAILURES = 2;
    
    /**
     * Thực hiện request đến Gemini với fallback
     * Nếu key hiện tại fail, tự động chuyển sang key tiếp theo
     */
    public String executeWithFallback(String requestBody, boolean isEmbedding) {
        List<String> apiKeys = config.getAllApiKeys();
        
        if (apiKeys.isEmpty()) {
            log.error("❌ Không có API key nào được cấu hình");
            throw new IllegalStateException("No Gemini API keys configured");
        }
        
        if (!config.getFallbackEnabled()) {
            log.warn("⚠️ Fallback disabled, chỉ dùng key đầu tiên");
            return executeRequest(requestBody, apiKeys.get(0), isEmbedding);
        }
        
        int totalKeys = apiKeys.size();
        int attemptsRemaining = totalKeys * config.getRetryCount();
        
        while (attemptsRemaining > 0) {
            int keyIndex = currentKeyIndex.getAndIncrement() % totalKeys;
            String apiKey = apiKeys.get(keyIndex);
            
            try {
                log.debug("🔑 Thử API key #{} ({}/{})", 
                    keyIndex + 1, totalKeys - attemptsRemaining + 1, totalKeys * config.getRetryCount());
                
                String response = executeRequest(requestBody, apiKey, isEmbedding);
                
                log.info("✅ Request thành công với key #{}", keyIndex + 1);
                return response;
                
            } catch (RestClientException e) {
                attemptsRemaining--;
                log.warn("⚠️ Key #{} fail: {}. Remaining attempts: {}", 
                    keyIndex + 1, e.getMessage(), attemptsRemaining);
                
                if (attemptsRemaining == 0) {
                    log.error("❌ Tất cả {} API keys đều fail", totalKeys);
                    throw new RuntimeException(
                        String.format("All %d Gemini API keys failed", totalKeys), e);
                }
                
                // Short delay before retry
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during fallback retry", ie);
                }
            }
        }
        
        throw new RuntimeException("Exhausted all retry attempts");
    }
    
    /**
     * Thực hiện request thực tế đến Gemini
     */
    private String executeRequest(String requestBody, String apiKey, boolean isEmbedding) {
        String endpoint = isEmbedding 
            ? config.getEmbeddingEndpoint(apiKey) 
            : config.getGenerateEndpoint(apiKey);
        
        log.debug("📤 Gửi request tới: {}", endpoint.replaceAll("key=.*", "key=***"));
        
        return restTemplate.postForObject(
            endpoint,
            requestBody,
            String.class
        );
    }
    
    /**
     * Lấy số lượng API keys hiện tại
     */
    public int getAvailableKeyCount() {
        return config.getAllApiKeys().size();
    }
    
    /**
     * Lấy API key hiện tại (cho debugging)
     */
    public String getCurrentKeyInfo() {
        List<String> keys = config.getAllApiKeys();
        if (keys.isEmpty()) {
            return "No keys configured";
        }
        int index = currentKeyIndex.get() % keys.size();
        return String.format("Key %d/%d", index + 1, keys.size());
    }
}
