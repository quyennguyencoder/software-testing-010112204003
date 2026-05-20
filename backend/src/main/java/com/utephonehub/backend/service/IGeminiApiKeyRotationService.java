package com.utephonehub.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quản lý và tự động xoay vòng API keys Gemini
 * Khi API key bị hết quota (lỗi 429) → tự động chuyển sang key khác
 * 
 * Cách hoạt động:
 * 1. Cấu hình: 1 key chính + 10 key dự phòng
 * 2. Khi API trả về lỗi 429 → tự động chuyển sang key tiếp theo
 * 3. Ghi lại số lỗi của mỗi key để theo dõi
 * 4. Thử tất cả keys (tối đa 3 lần) trước khi báo lỗi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IGeminiApiKeyRotationService {
    
    @Value("${gemini.api.key}")
    private String primaryKey;
    
    @Value("${gemini.api.key.1:}")
    private String fallbackKey1;
    
    @Value("${gemini.api.key.2:}")
    private String fallbackKey2;
    
    @Value("${gemini.api.key.3:}")
    private String fallbackKey3;
    
    @Value("${gemini.api.key.4:}")
    private String fallbackKey4;
    
    @Value("${gemini.api.key.5:}")
    private String fallbackKey5;
    
    @Value("${gemini.api.key.6:}")
    private String fallbackKey6;
    
    @Value("${gemini.api.key.7:}")
    private String fallbackKey7;
    
    @Value("${gemini.api.key.8:}")
    private String fallbackKey8;
    
    @Value("${gemini.api.key.9:}")
    private String fallbackKey9;
    
    @Value("${gemini.api.key.10:}")
    private String fallbackKey10;
    
    @Value("${gemini.enable.fallback:true}")
    private boolean enableFallback;
    
    @Value("${gemini.rotate.key.on.error:true}")
    private boolean rotateKeyOnError;
    
    // Current key index (0 = primary, 1-10 = fallback)
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);
    
    // Track errors per key
    private final Map<Integer, KeyStats> keyStats = new ConcurrentHashMap<>();
    
    // List of all keys
    private List<String> allKeys;
    
    /**
     * Khởi tạo danh sách keys sau khi load từ .env
     * Loại bỏ các keys trống, tính tổng số keys có sẵn
     */
    public void initializeKeys() {
        allKeys = new ArrayList<>();
        allKeys.add(primaryKey);
        allKeys.addAll(Arrays.asList(
            fallbackKey1, fallbackKey2, fallbackKey3, fallbackKey4, fallbackKey5,
            fallbackKey6, fallbackKey7, fallbackKey8, fallbackKey9, fallbackKey10
        ));
        
        // Remove empty keys
        allKeys.removeIf(String::isBlank);
        
        log.info("🔑 Gemini API Keys initialized: {} keys available", allKeys.size());
        
        // Initialize stats
        for (int i = 0; i < allKeys.size(); i++) {
            keyStats.put(i, new KeyStats(i));
        }
    }
    
    /**
     * Lấy API key đang dùng hiện tại
     * Cập nhật thời gian sử dụng cuối cùng
     */
    public String getCurrentKey() {
        if (allKeys == null || allKeys.isEmpty()) {
            initializeKeys();
        }
        
        int index = currentKeyIndex.get();
        if (index >= allKeys.size()) {
            index = 0;
            currentKeyIndex.set(0);
        }
        
        String key = allKeys.get(index);
        KeyStats stats = keyStats.get(index);
        stats.lastUsed = LocalDateTime.now();
        
        log.debug("🔑 Using key #{} (errors: {})", index, stats.errorCount);
        return key;
    }
    
    /**
     * Chuyển sang key tiếp theo (khi key hiện tại hết quota)
     * Cơ chế: xoay vòng lần lượt qua tất cả keys
     */
    public void rotateToNextKey(String errorMessage) {
        if (!enableFallback || allKeys.size() <= 1) {
            log.warn("⚠️ Fallback disabled hoặc chỉ 1 key");
            return;
        }
        
        int currentIndex = currentKeyIndex.get();
        KeyStats stats = keyStats.get(currentIndex);
        stats.errorCount++;
        
        log.warn("❌ Key #{} lỗi: {} (error count: {})", currentIndex, errorMessage, stats.errorCount);
        
        // Rotate sang key tiếp theo
        int nextIndex = (currentIndex + 1) % allKeys.size();
        currentKeyIndex.set(nextIndex);
        
        KeyStats nextStats = keyStats.get(nextIndex);
        log.info("🔄 Fallback sang key #{} (errors: {})", nextIndex, nextStats.errorCount);
    }
    
    /**
     * Gọi API có hỗ trợ xoay vòng keys
     * Tự động chuyển sang key khác nếu gặp lỗi 429 (hết quota)
     * @param function Hàm gọi API cần thực hiện
     * @param operationName Tên hoạt động (cho log)
     */
    public <T> T callWithFallback(ApiCallFunction<T> function, String operationName) throws Exception {
        if (allKeys == null || allKeys.isEmpty()) {
            initializeKeys();
        }
        
        int maxAttempts = Math.min(allKeys.size(), 3); // Thử tối đa 3 keys
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                String key = getCurrentKey();
                log.debug("📤 Attempt #{} - {} với key #{}", attempt + 1, operationName, currentKeyIndex.get());
                return function.call(key);
                
            } catch (Exception e) {
                lastException = e;
                String errorMsg = e.getMessage();
                
                // Kiểm tra xem có phải lỗi hết quota không
                if (errorMsg != null && (
                    errorMsg.contains("429") || 
                    errorMsg.contains("RESOURCE_EXHAUSTED") ||
                    errorMsg.contains("quota") ||
                    errorMsg.contains("quota exceeded")
                )) {
                    log.warn("⚠️ Key này hết quota, chuyển sang key tiếp theo...");
                    rotateToNextKey(errorMsg);
                } else {
                    // Lỗi khác (không phải hết quota), không chuyển key
                    throw e;
                }
            }
        }
        
        // Đã thử hết tất cả keys
        log.error("❌ Tất cả {} keys đều lỗi sau {} lần thử", allKeys.size(), maxAttempts);
        throw new RuntimeException("Tất cả Gemini API keys đều hết quota. Vui lòng thêm API key mới", lastException);
    }
    
    /**
     * Ghi nhận lỗi cho key hiện tại
     * Nếu là lỗi 429, tự động chuyển sang key khác
     */
    public void reportError(String errorMessage) {
        if (!rotateKeyOnError) {
            return;
        }
        
        int currentIndex = currentKeyIndex.get();
        if (errorMessage != null && errorMessage.contains("429")) {
            rotateToNextKey(errorMessage);
        }
    }
    
    /**
     * Lấy thống kê chi tiết về tất cả keys
     * Gồm: số lỗi, thời gian dùng cuối, trạng thái, key hiện tại
     */
    public Map<String, Object> getKeyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        for (int i = 0; i < allKeys.size(); i++) {
            KeyStats keyStats = this.keyStats.get(i);
            Map<String, Object> stat = new HashMap<>();
            stat.put("keyIndex", i);
            stat.put("isCurrent", i == currentKeyIndex.get());
            stat.put("errorCount", keyStats.errorCount);
            stat.put("lastUsed", keyStats.lastUsed);
            stat.put("status", keyStats.errorCount == 0 ? "✅ OK" : "⚠️ ERROR");
            
            stats.put("key_" + i, stat);
        }
        
        stats.put("currentIndex", currentKeyIndex.get());
        stats.put("totalKeys", allKeys.size());
        stats.put("fallbackEnabled", enableFallback);
        
        return stats;
    }
    
    /**
     * Xóa tất cả dữ liệu lỗi, trở về key đầu tiên
     * (Hàm dành cho admin)
     */
    public void resetStats() {
        keyStats.forEach((key, stats) -> stats.errorCount = 0);
        currentKeyIndex.set(0);
        log.info("🔄 API Key stats reset");
    }
    
    /**
     * Interface cho hàm gọi API
     * Nhận API key vào, thực hiện gọi API, trả về kết quả
     */
    @FunctionalInterface
    public interface ApiCallFunction<T> {
        T call(String apiKey) throws Exception;
    }
    
    /**
     * Lưu trữ thông tin thống kê cho mỗi API key
     * Gồm: chỉ số key, số lỗi gặp phải, thời gian dùng lần cuối
     */
    private static class KeyStats {
        int keyIndex;
        int errorCount = 0;
        LocalDateTime lastUsed = LocalDateTime.now();
        
        KeyStats(int keyIndex) {
            this.keyIndex = keyIndex;
        }
    }
}
