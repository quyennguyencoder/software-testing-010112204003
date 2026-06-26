package com.phonehub.backend.service.impl;

import com.phonehub.backend.exception.EmailServiceException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:3000");
    }

    // ====================================================================================
    // TEST: sendVerificationEmail
    // ====================================================================================
    // Kiểm tra hàm sendVerificationEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendVerificationEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        when(templateEngine.process(eq("registration-email"), any(Context.class)))
                .thenReturn("<html><body><h1>Xin chào Nguyễn Văn A, đây là mã xác nhận...</h1></body></html>");
        assertDoesNotThrow(() -> {
            emailService.sendVerificationEmail("vanA@gmail.com", "Nguyễn Văn A");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("registration-email"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage); 
    }
    // Kiểm tra hàm sendVerificationEmail() khi template tạo ra nội dung rỗng, hệ thống phải ném ra EmailServiceException
    @Test
    void sendVerificationEmail_WhenTemplateProducesEmptyContent_ShouldThrowEmailServiceException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        when(templateEngine.process(eq("registration-email"), any(Context.class))).thenReturn("");
        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendVerificationEmail("vanA@gmail.com", "Nguyễn Văn A");
        });
        assertEquals("Không thể gửi email", exception.getMessage());
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("registration-email"), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class)); // Hàm send không bao giờ được gọi
    }
    // ====================================================================================
    // TEST: sendOtpEmail
    // ====================================================================================
    // Kiểm tra hàm sendOtpEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendOtpEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-otp"), any(Context.class)))
                .thenReturn("<html><body>Mã OTP đặt lại mật khẩu của sếp là: 123456</body></html>");
        assertDoesNotThrow(() -> {
            emailService.sendOtpEmail("test-otp@gmail.com", "123456");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("password-reset-otp"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    // ====================================================================================
    // TEST: sendRegistrationOtpEmail // hàm này k sài ở frontend
    // ====================================================================================
    // Kiểm tra hàm sendRegistrationOtpEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendRegistrationOtpEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("registration-otp-email"), any(Context.class)))
                .thenReturn("<html><body>Chào Cương, mã OTP đăng ký là: 654321</body></html>");
        assertDoesNotThrow(() -> {
            emailService.sendRegistrationOtpEmail("cuongnc@gmail.com", "Nguyễn Chí Cương", "654321");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("registration-otp-email"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    
}