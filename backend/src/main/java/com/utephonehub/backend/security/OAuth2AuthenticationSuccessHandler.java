package com.utephonehub.backend.security;

import com.utephonehub.backend.dto.response.auth.AuthResponse;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthServiceImpl authService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/google/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String email = oidcUser.getEmail();

            if (email == null) {
                log.error("OAuth2AuthenticationSuccessHandler: email is null");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Đã có lỗi xảy ra với việc đăng nhập Google, vui lòng thử lại");
                return;
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.error("User not found after successful Google login, email={}", email);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Đã có lỗi xảy ra với việc đăng nhập Google, vui lòng thử lại");
                return;
            }

            var authResponse = authService.buildAuthResponse(user);

            String targetUrl = buildRedirectUrl(authResponse);

            log.info("Google login successful for user id: {}, redirecting to {}", user.getId(), targetUrl);
            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("Error in OAuth2AuthenticationSuccessHandler: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã có lỗi xảy ra với việc đăng nhập Google, vui lòng thử lại");
        }
    }

    private String buildRedirectUrl(AuthResponse authResponse) {
        StringBuilder url = new StringBuilder(redirectUri);
        if (!redirectUri.contains("#")) {
            url.append("#");
        } else if (!redirectUri.endsWith("#") && !redirectUri.endsWith("&")) {
            url.append("&");
        }

        url.append("accessToken=").append(urlEncode(authResponse.getAccessToken()))
                .append("&refreshToken=").append(urlEncode(authResponse.getRefreshToken()))
                .append("&tokenType=").append(urlEncode(authResponse.getTokenType()))
                .append("&expiresIn=").append(authResponse.getExpiresIn());

        return url.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}


