package com.utephonehub.backend.exception;

import com.utephonehub.backend.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<?>> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        log.error("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.forbidden(ex.getMessage()));
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            Exception ex, WebRequest request) {
        log.error("Access Denied: {}", ex.getMessage());
        String message = ex.getMessage() != null ? ex.getMessage() : "Access Denied";
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.forbidden(message));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleConflictException(
            ConflictException ex, WebRequest request) {
        log.error("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.conflict(ex.getMessage()));
    }

    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailServiceException(
            EmailServiceException ex, WebRequest request) {
        log.error("Email service error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError(ex.getMessage()));
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiResponse<?>> handleOutOfStockException(
            OutOfStockException ex, WebRequest request) {
        log.error("Out of stock: {}", ex.getMessage());
        Map<String, Object> data = new HashMap<>();
        data.put("productId", ex.getProductId());
        data.put("productName", ex.getProductName());
        data.put("requestedQuantity", ex.getRequestedQuantity());
        data.put("availableStock", ex.getAvailableStock());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), data));
    }

    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<ApiResponse<?>> handleVersionConflictException(
            VersionConflictException ex, WebRequest request) {
        log.error("Version conflict: {}", ex.getMessage());
        Map<String, Object> data = new HashMap<>();
        if (ex.getCurrentQuantity() != null) {
            data.put("currentQuantity", ex.getCurrentQuantity());
            data.put("requestedQuantity", ex.getRequestedQuantity());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(409, ex.getMessage(), data));
    }

    @ExceptionHandler(MaxQuantityExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxQuantityExceededException(
            MaxQuantityExceededException ex, WebRequest request) {
        log.error("Max quantity exceeded: {}", ex.getMessage());
        Map<String, Object> data = new HashMap<>();
        data.put("maxQuantity", ex.getMaxQuantity());
        data.put("requestedQuantity", ex.getRequestedQuantity());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), data));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        // Include error message in response for debugging (consider removing in production)
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalServerError(errorMessage));
    }
}

