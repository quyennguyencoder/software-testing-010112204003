package com.utephonehub.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String targetUrl;
        String errorMessage = exception.getMessage();

        // Check if the error is due to a locked account
        if (errorMessage != null && errorMessage.contains("khóa")) {
            targetUrl = frontendBaseUrl + "/account-locked";
            log.info("Redirecting locked user to: {}", targetUrl);
        } else {
            // Generic error - redirect to login with error parameter
            String encodedError = URLEncoder.encode(
                    errorMessage != null ? errorMessage : "Đăng nhập Google thất bại",
                    StandardCharsets.UTF_8);
            targetUrl = frontendBaseUrl + "/login?error=" + encodedError;
            log.info("Redirecting to login with error: {}", targetUrl);
        }

        response.sendRedirect(targetUrl);
    }
}
