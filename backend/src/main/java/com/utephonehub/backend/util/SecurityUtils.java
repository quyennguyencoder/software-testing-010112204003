package com.utephonehub.backend.util;

import com.utephonehub.backend.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations.
 * Provides methods to extract user information from JWT tokens.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Extracts the user ID from the Authorization header in the request.
     *
     * @param request the HTTP request containing the Authorization header
     * @return the user ID extracted from the JWT token
     * @throws UnauthorizedException if the token is invalid or missing
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        }
        throw new UnauthorizedException("Token không hợp lệ hoặc đã hết hạn");
    }

    /**
     * Tries to get the current user ID from the Authorization header.
     * <p>
     * Khác với {@link #getCurrentUserId(HttpServletRequest)} là method này
     * KHÔNG ném ra exception nếu token không hợp lệ hoặc thiếu, mà sẽ trả về null.
     *
     * @param request the HTTP request containing the Authorization header
     * @return the user ID if authenticated, or null if not authenticated/invalid token
     */
    // Note: implementation below uses token extraction and validation directly.

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request containing the Authorization header
     * @return the JWT token string, or null if not present
     */
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Get client IP address from request
     */
    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    /**
     * Returns the user id if the request contains a valid JWT token, otherwise null.
     * This method does not throw an exception for unauthenticated requests.
     */
    public Long getUserIdIfAuthenticated(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        } catch (Exception ex) {
            // swallow and return null for unauthenticated
            return null;
        }
        return null;
    }
}
