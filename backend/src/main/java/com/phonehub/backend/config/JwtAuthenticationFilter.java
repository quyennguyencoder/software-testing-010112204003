package com.phonehub.backend.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phonehub.backend.entity.User;
import com.phonehub.backend.enums.UserStatus;
import com.phonehub.backend.repository.UserRepository;
import com.phonehub.backend.util.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                log.debug("No JWT token found in request");
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT token found, validating...");
            if (!jwtTokenProvider.validateToken(jwt)) {
                log.warn("JWT token validation failed");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
            log.debug("User ID extracted from token: {}", userId);

            if (userId == null) {
                log.warn("Could not extract user ID from token");
                filterChain.doFilter(request, response);
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found in database with ID: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // Check user status
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("User {} is not ACTIVE. Status: {}", user.getEmail(), user.getStatus());
                filterChain.doFilter(request, response);
                return;
            }

            // ✅ Set authentication - Add "ROLE_" prefix for hasRole() to work correctly
            try {
                String roleName = user.getRole().name();
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(authority));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ Successfully authenticated user: {} with role: {}", user.getEmail(), roleName);
            } catch (Exception e) {
                log.error("❌ Failed to create authentication token for user: {}", user.getEmail(), e);
                filterChain.doFilter(request, response);
                return;
            }

        } catch (Exception e) {
            log.error("❌ Unexpected error in JWT filter: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
