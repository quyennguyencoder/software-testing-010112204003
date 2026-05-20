package com.utephonehub.backend.service;

/**
 * Interface for Email Service operations
 */
public interface IEmailService {
    
    /**
     * Send verification email to user
     * @param email User email
     * @param fullName User full name
     */
    void sendVerificationEmail(String email, String fullName);
    
    /**
     * Send OTP email for password reset
     * @param email User email
     * @param otp OTP code
     */
    void sendOtpEmail(String email, String otp);

    /**
     * Send OTP email for email verification after registration
     * @param email User email
     * @param fullName User full name
     * @param otp OTP code
     */
    void sendRegistrationOtpEmail(String email, String fullName, String otp);
    
    /**
     * Send password reset confirmation email
     * @param email User email
     * @param fullName User full name
     */
    void sendPasswordResetEmail(String email, String fullName);
    
    /**
     * Send registration welcome email
     * @param email User email
     * @param fullName User full name
     */
    void sendRegistrationEmail(String email, String fullName);
    
    /**
     * Send order payment success email
     * @param email Customer email
     * @param orderCode Order code
     * @param orderTotal Total amount
     * @param recipientName Recipient name
     * @param paymentMethod Payment method
     */
    void sendOrderPaymentSuccessEmail(String email, String orderCode, 
                                      java.math.BigDecimal orderTotal, 
                                      String recipientName, 
                                      String paymentMethod);
}
