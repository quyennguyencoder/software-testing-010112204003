package com.utephonehub.backend.exception;

/**
 * Exception thrown when email service operations fail
 * HTTP Status: 500 Internal Server Error
 */
public class EmailServiceException extends RuntimeException {
    public EmailServiceException(String message) {
        super(message);
    }

    public EmailServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
