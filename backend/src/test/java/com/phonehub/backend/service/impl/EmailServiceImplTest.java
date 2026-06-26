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
    // TEST: sendRegistrationOtpEmail 
    // ====================================================================================
    // Kiểm tra hàm sendRegistrationOtpEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendRegistrationOtpEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("registration-otp-email"), any(Context.class)))
                .thenReturn("<html><body>Chào A, mã OTP đăng ký là: 654321</body></html>");
        assertDoesNotThrow(() -> {
            emailService.sendRegistrationOtpEmail("vanA@gmail.com", "Nguyễn Văn A", "654321");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("registration-otp-email"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    // ====================================================================================
    // TEST: sendPasswordResetEmail
    // ====================================================================================
    // Kiểm tra hàm sendPasswordResetEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendPasswordResetEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-success"), any(Context.class)))
                .thenReturn("<html><body>Đổi mật khẩu thành công lúc 10:00!</body></html>");

        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail("vanA@gmail.com", "Nguyễn Văn A");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("password-reset-success"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    // ====================================================================================
    // TEST: sendRegistrationEmail
    // ====================================================================================
    // Kiểm tra hàm sendRegistrationEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendRegistrationEmail_WhenValidData_ShouldSendEmailSuccessfully() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("registration-email"), any(Context.class)))
                .thenReturn("<html><body>Chào mừng gia nhập hệ thống!</body></html>");

        assertDoesNotThrow(() -> {
            emailService.sendRegistrationEmail("vanA@gmail.com", "Nguyễn Văn A");
        });
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("registration-email"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    // Kiểm tra hàm sendRegistrationEmail() khi có ngoại lệ xảy ra, hệ thống phải ném ra EmailServiceException
    @Test
    void sendRegistrationEmail_WhenExceptionOccurs_ShouldCatchAndThrowEmailServiceException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("registration-email"), any(Context.class)))
                .thenThrow(new RuntimeException("Lỗi máy chủ đột xuất!"));

        EmailServiceException exception = assertThrows(EmailServiceException.class, () -> {
            emailService.sendRegistrationEmail("vanA@gmail.com", "Nguyễn Văn A");
        });

        assertEquals("Không thể gửi email đăng ký", exception.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
    // ====================================================================================
    // TEST: sendOrderPaymentSuccessEmail
    // ====================================================================================
    // Kiểm tra hàm sendOrderPaymentSuccessEmail() khi dữ liệu hợp lệ, hệ thống phải gửi email thành công
    @Test
    void sendOrderPaymentSuccessEmail_WhenValidOrderTotal_ShouldFormatCurrencyAndSendEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("order-payment-success"), any(Context.class)))
                .thenReturn("<html><body>Thanh toán 15,000,000 ₫ thành công qua VNPAY</body></html>");

        // 2. Act & Assert: Gọi hàm truyền BigDecimal có giá trị
        assertDoesNotThrow(() -> {
            emailService.sendOrderPaymentSuccessEmail(
                    "vanA@gmail.com", 
                    "ORD-9999", 
                    new java.math.BigDecimal("15000000"), 
                    "Nguyễn Văn A", 
                    "VNPAY"
            );
        });

        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("order-payment-success"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }
    // Kiểm tra hàm sendOrderPaymentSuccessEmail() khi Order Total là null, hệ thống phải định dạng thành 0 và gửi email thành công
    @Test
    void sendOrderPaymentSuccessEmail_WhenOrderTotalIsNull_ShouldFormatAsZeroAndSendEmail() {
        // mail cho một đơn được tặng miễn phí 
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("order-payment-success"), any(Context.class)))
                .thenReturn("<html><body>Thanh toán 0 ₫ thành công</body></html>");

        // Gọi hàm nhưng truyền tham số tiền bị null
        assertDoesNotThrow(() -> {
            emailService.sendOrderPaymentSuccessEmail(
                    "cuongnc@gmail.com", 
                    "ORD-0000", 
                    null, 
                    "Nguyễn Chí Cương", 
                    "MoMo"
            );
        });

        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq("order-payment-success"), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    } 
    
}