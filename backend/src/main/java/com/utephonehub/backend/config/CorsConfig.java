// src/main/java/com/utephonehub/backend/config/CorsConfig.java
package com.utephonehub.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration Cấu hình CORS toàn diện để
 * fix lỗi Swagger UI và frontend
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS settings for the application
     * 
     * @return CorsConfigurationSource with allowed origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Cho phép tất cả localhost với các port phổ biến của frontend dev server
        // Bao gồm cả Next.js default port (3000) và các port khác
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://172.*:*",
            "http://192.168.*:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));
        
        // Cho phép các HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Cho phép tất cả headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Cho phép gửi credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight request trong 1 giờ
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
