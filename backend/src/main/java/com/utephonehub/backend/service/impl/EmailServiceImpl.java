package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.exception.EmailServiceException;
import com.utephonehub.backend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Qualifier("emailTemplateEngine")
    private final SpringTemplateEngine templateEngine;

    @Value("${frontend.url}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Helper method to send HTML email using Thymeleaf template
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }

            // Add common variables
            String loginUrl = (frontendUrl != null ? frontendUrl : "http://localhost:3000") + "/login";
            String orderTrackingUrl = (frontendUrl != null ? frontendUrl : "http://localhost:3000")
                    + "/user?tab=orders";
            context.setVariable("loginUrl", loginUrl);
            context.setVariable("orderTrackingUrl", orderTrackingUrl);

            log.debug("Processing template: {} for email to: {}", templateName, to);
            String htmlContent = templateEngine.process(templateName, context);

            if (htmlContent == null || htmlContent.trim().isEmpty()) {
                log.error("Template {} produced empty content for email to: {}", templateName, to);
                throw new EmailServiceException("Template produced empty content: " + templateName);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML email sent successfully to: {} with template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {} with template {}: {}", to, templateName, e.getMessage(), e);
            throw new EmailServiceException("Không thể gửi email", e);
        }
    }

    @Override
    public void sendVerificationEmail(String email, String fullName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);

        sendHtmlEmail(email, "Xác thực tài khoản UTE Phone Hub",
                "registration-email", variables);
    }

    @Override
    public void sendOtpEmail(String email, String otp) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("otp", otp);

        sendHtmlEmail(email, "Mã OTP đặt lại mật khẩu UTE Phone Hub",
                "password-reset-otp", variables);
    }

    @Override
    public void sendRegistrationOtpEmail(String email, String fullName, String otp) {
        log.info("Sending registration OTP email to: {} for user: {}", email, fullName);
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("otp", otp);

        sendHtmlEmail(email, "Mã OTP xác thực email - UTE Phone Hub",
                "registration-otp-email", variables);
    }

    @Override
    public void sendPasswordResetEmail(String email, String fullName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);
        variables.put("resetTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));

        sendHtmlEmail(email, "Mật khẩu đã được đặt lại thành công - UTE Phone Hub",
                "password-reset-success", variables);
    }

    @Override
    public void sendRegistrationEmail(String email, String fullName) {
        log.info("Sending registration email to: {} for user: {}", email, fullName);
        Map<String, Object> variables = new HashMap<>();
        variables.put("fullName", fullName);

        try {
            sendHtmlEmail(email, "Chào mừng đến với UTE Phone Hub",
                    "registration-email", variables);
            log.info("Registration email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", email, e.getMessage(), e);
            throw new EmailServiceException("Không thể gửi email đăng ký", e);
        }
    }

    @Override
    public void sendOrderPaymentSuccessEmail(String email, String orderCode,
            BigDecimal orderTotal,
            String recipientName,
            String paymentMethod) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderCode", orderCode);
        variables.put("orderTotal", formatCurrency(orderTotal));
        variables.put("recipientName", recipientName);
        variables.put("paymentMethod", paymentMethod);
        variables.put("paymentTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));

        sendHtmlEmail(email, "Thanh toán thành công - UTE Phone Hub",
                "order-payment-success", variables);
    }

    /**
     * Format currency to Vietnamese Dong format
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return String.format("%,d ₫", amount.longValue());
    }
}
