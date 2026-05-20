package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "API kiểm tra trạng thái hệ thống")
public class HealthController {

    @GetMapping
    @Operation(summary = "Kiểm tra trạng thái API", description = "Trả về trạng thái hoạt động của API")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "UTE Phone Hub Backend");
        data.put("version", "1.0.0");
        return ResponseEntity.ok(ApiResponse.success("API is healthy and running", data));
    }
}

