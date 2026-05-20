package com.utephonehub.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utephonehub.backend.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service để tạo embedding sử dụng Gemini Embedding Model
 * Tối ưu chi phí: Cache embedding, batch processing
 * Support fallback API keys để tránh rate limit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IGeminiEmbeddingService {
    
    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final IGeminiFallbackService fallbackService;
    private final ObjectMapper objectMapper;
    
    // Cache embedding để tối ưu chi phí
    private final Map<String, List<Double>> embeddingCache = new HashMap<>();
    
    /**
     * Lấy embedding cho một text (với fallback API keys)
     * @param text Văn bản cần tạo embedding
     * @return Vector embedding (1536 dimensions)
     */
    public List<Double> getEmbedding(String text) {
        if (embeddingCache.containsKey(text)) {
            log.debug("✅ Sử dụng embedding từ cache cho text: {}", text.substring(0, Math.min(50, text.length())));
            return embeddingCache.get(text);
        }
        
        try {
            Map<String, Object> requestBody = Map.of(
                "requests", List.of(
                    Map.of("text", text)
                )
            );
            
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("📤 Gửi embedding request, {} fallback keys available", 
                fallbackService.getAvailableKeyCount());
            
            // Sử dụng fallback service với xoay vòng API keys
            String responseJson = fallbackService.executeWithFallback(requestJson, true);
            
            JsonNode responseNode = objectMapper.readTree(responseJson);
            JsonNode embeddingValues = responseNode.path("embeddings").get(0).path("values");
            
            List<Double> embedding = new ArrayList<>();
            embeddingValues.forEach(node -> embedding.add(node.asDouble()));
            
            // Lưu vào cache
            embeddingCache.put(text, embedding);
            log.debug("💾 Embedding cached cho text: {}", text.substring(0, Math.min(50, text.length())));
            
            return embedding;
        } catch (Exception e) {
            log.error("❌ Lỗi tạo embedding: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Tính độ tương tự cosine giữa 2 vector
     * @param vec1 Vector 1
     * @param vec2 Vector 2
     * @return Độ tương tự (0-1)
     */
    public Double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.isEmpty() || vec2.isEmpty() || vec1.size() != vec2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Xóa cache (có thể gọi periodic để tiết kiệm memory)
     */
    public void clearCache() {
        embeddingCache.clear();
        log.info("🧹 Embedding cache đã được xóa");
    }
    
    /**
     * Lấy số lượng embedding đã cache
     */
    public int getCacheSize() {
        return embeddingCache.size();
    }
}
