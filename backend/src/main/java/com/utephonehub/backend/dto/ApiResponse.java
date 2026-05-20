package com.utephonehub.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper following RESTful best practices
 * @param <T> Type of response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null fields in JSON response
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     */
    private boolean success;

    /**
     * HTTP status code (mirrors ResponseEntity status for convenience)
     */
    private int status;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Response payload (null for errors without details)
     */
    private T data;

    /**
     * Timestamp in ISO-8601 format with UTC timezone
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime timestamp;

    // ==================== Success Responses ====================

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> noContent(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(204)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== Error Responses ====================

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return error(409, message);
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(500, message);
    }
}

