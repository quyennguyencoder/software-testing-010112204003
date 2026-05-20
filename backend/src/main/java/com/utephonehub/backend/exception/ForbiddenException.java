package com.utephonehub.backend.exception;

/**
 * Exception thrown when user is authenticated but lacks required permissions
 * HTTP Status: 403 Forbidden
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
