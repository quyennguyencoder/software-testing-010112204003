// src/main/java/com/utephonehub/backend/config/SecurityConfig.java
package com.phonehub.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.phonehub.backend.security.CustomOidcUserService;
import com.phonehub.backend.security.CustomOAuth2FailureHandler;
import com.phonehub.backend.security.OAuth2AuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CorsConfigurationSource corsConfigurationSource;
        private final CustomOidcUserService customOidcUserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // ✅ CORS MUST BE FIRST - Apply CORS configuration
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                                // ✅ Disable CSRF for REST APIs
                                .csrf(AbstractHttpConfigurer::disable)

                                // ✅ Stateless session for JWT
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                // ✅ Configure authorization rules
                                .authorizeHttpRequests(auth -> auth

                                                // ========================================
                                                // PUBLIC ENDPOINTS (No Authentication Required)
                                                // ========================================

                                                // Swagger UI and OpenAPI docs
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/favicon.ico")
                                                .permitAll()
                                                // Cho phép các endpoint OAuth2 (Google) - phải đặt trước các rule khác
                                                .requestMatchers(
                                                                "/oauth2/authorization/**",
                                                                "/login/oauth2/code/**",
                                                                "/.well-known/**")
                                                .permitAll()
                                                // Cho phép truy cập tự do vào các API xác thực
                                                .requestMatchers(
                                                                "/api/v1/auth/**",
                                                                "/api/v1/health/**",
                                                                "/api/v1/guest-cart/**")
                                                .permitAll()

                                                // Health check endpoints
                                                .requestMatchers(
                                                                "/api/v1/payments/**")
                                                .permitAll()
                                                // Cho phép Chatbot endpoint (public - không cần xác thực)
                                                .requestMatchers(
                                                                "/api/v1/chatbot-assistant/**")
                                                .permitAll()
                                                // Cho phép truy cập tự do vào API địa chỉ hành chính (OpenAPI Vietnam)
                                                .requestMatchers(
                                                                "/api/v1/locations/**")
                                                .permitAll() // Cho phép truy cập tự do vào API danh mục và thương hiệu
                                                             // (public - chỉ GET)
                                                .requestMatchers(
                                                                "/api/v1/public/**")
                                                .permitAll()

                                                // Payment endpoints (VNPay callbacks)
                                                .requestMatchers(
                                                                "/api/payments/**",
                                                                "/api/v1/payments/**")
                                                .permitAll()

                                                // Public product browsing endpoints
                                                .requestMatchers(
                                                                "/api/v1/products/**",
                                                                "/api/v1/categories/**",
                                                                "/api/v1/brands/**")
                                                .permitAll()
                                                // Cho phép public POST /products/filter (public search)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/v1/products/filter")
                                                .permitAll()
                                                // Cho phép public POST /products/compare (so sánh sản phẩm -
                                                // ProductViewController, public API)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/v1/products/compare")
                                                .permitAll()
                                                // Cho phép public POST /products/*/restore (sẽ override bên dưới)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/v1/products/*/restore")
                                                .hasRole("ADMIN")
                                                // Yêu cầu ADMIN cho POST /products (create - exact match)
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/v1/products")
                                                .hasRole("ADMIN")
                                                // Yêu cầu ADMIN cho PUT (update product)
                                                .requestMatchers(org.springframework.http.HttpMethod.PUT,
                                                                "/api/v1/products/**")
                                                .hasRole("ADMIN")
                                                // Yêu cầu ADMIN cho DELETE (soft delete)
                                                .requestMatchers(org.springframework.http.HttpMethod.DELETE,
                                                                "/api/v1/products/**")
                                                .hasRole("ADMIN")
                                                // Yêu cầu ADMIN cho PATCH (update stock)
                                                .requestMatchers(org.springframework.http.HttpMethod.PATCH,
                                                                "/api/v1/products/**")
                                                .hasRole("ADMIN")
                                                // Cho phép GET public cho products (search, filter, list, detail)
                                                .requestMatchers(
                                                                org.springframework.http.HttpMethod.GET,
                                                                "/api/v1/products",
                                                                "/api/v1/products/**")
                                                .permitAll()
                                                // Yêu cầu ADMIN cho các API quản lý danh mục, thương hiệu, sản phẩm,
                                                // người dùng
                                                .requestMatchers(
                                                                "/api/v1/promotions/**")
                                                .permitAll()

                                                // ========================================
                                                // ADMIN ENDPOINTS (Admin Role Required)
                                                // ========================================

                                                // Admin Order Management (Module 07 - Admin)
                                                // Sử dụng hasRole(\"ADMIN\") để đồng bộ với các endpoint admin khác
                                                // (ROLE_ADMIN)
                                                .requestMatchers("/api/v1/admin/orders/**").hasRole("ADMIN")

                                                // Admin Chatbot Management - Bật/tắt chatbot (phòng tấn công/tốn
                                                // request)
                                                .requestMatchers("/api/v1/admin/chatbot/**").hasRole("ADMIN")

                                                // Other admin endpoints
                                                .requestMatchers(
                                                                "/api/v1/admin/users/**",
                                                                "/api/v1/admin/categories/**",
                                                                "/api/v1/admin/brands/**",
                                                                "/api/v1/admin/products/**",
                                                                "/api/v1/admin/users/**")
                                                .hasRole("ADMIN")
                                                // Cho phép truy cập tự do các API Promotion (tùy theo chính sách hiện
                                                // tại)
                                                .requestMatchers("/api/v1/admin/promotions/**").permitAll()
                                                .requestMatchers("/api/v1/promotions/**").permitAll()
                                                // Các request khác yêu cầu phải xác thực
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .oidcUserService(customOidcUserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                                .failureHandler(customOAuth2FailureHandler)
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/oauth2/authorization"))
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/login/oauth2/code/*")))
                                // Trả về 401 Unauthorized cho API requests thay vì redirect đến OAuth2 login
                                // Điều này giúp Swagger UI và các API clients xử lý lỗi đúng cách
                                .exceptionHandling(exceptions -> exceptions
                                                // Xử lý lỗi authentication (401) cho API
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                request -> request.getRequestURI().startsWith("/api/"))
                                                // Xử lý lỗi authorization/permission (403) cho API
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(HttpStatus.FORBIDDEN.value());
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write("{\"status\": 403, " +
                                                                        "\"message\": \"Access Denied: Bạn không có quyền truy cập tài nguyên này\"}");
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}