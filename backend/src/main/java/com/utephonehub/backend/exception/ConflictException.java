package com.utephonehub.backend.exception;

/**
 * Exception thrown when there is a conflict with existing resource
 * Typically used for duplicate entries (email, username, etc.)
 * HTTP Status: 409 Conflict
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
