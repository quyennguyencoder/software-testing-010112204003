
package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.order.TrackOrderRequest;
import com.utephonehub.backend.dto.response.order.PublicOrderTrackingResponse;
import com.utephonehub.backend.service.IPublicOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.http.ResponseEntity;
import org.springframework. web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Order Tracking", description = "API tra cứu đơn hàng công khai (không cần đăng nhập)")
@CrossOrigin(origins = {"http://localhost:3000", "https://utephonehub.com"})
public class PublicOrderController {
    
    private final IPublicOrderService publicOrderService;
    
    /**
     * POST /api/v1/public/orders/track
     * Tra cứu đơn hàng bằng mã đơn và email
     */
    @PostMapping("/track")
    @Operation(
        summary = "Tra cứu đơn hàng công khai",
        description = "Tra cứu trạng thái đơn hàng bằng mã đơn và email khách hàng.  Không cần đăng nhập."
    )
    public ResponseEntity<ApiResponse<PublicOrderTrackingResponse>> trackOrder(
            @Valid @RequestBody TrackOrderRequest request) {
        
        log.info("Public order tracking request: {}", request. getOrderCode());
        
        // Call service to track order
        PublicOrderTrackingResponse response = publicOrderService.trackOrder(request);
        
        return ResponseEntity.ok(
            ApiResponse. success(
                "Tra cứu đơn hàng thành công", 
                response
            )
        );
    }
    
    /**
     * GET /api/v1/public/orders/quick-track/{orderCode}
     * Tra cứu nhanh chỉ bằng mã đơn hàng
     */
    @GetMapping("/quick-track/{orderCode}")
    @Operation(
        summary = "Tra cứu nhanh bằng mã đơn",
        description = "Tra cứu trạng thái cơ bản chỉ bằng mã đơn hàng (thông tin hạn chế để bảo mật)"
    )
    public ResponseEntity<ApiResponse<PublicOrderTrackingResponse>> quickTrackOrder(
            @Parameter(description = "Mã đơn hàng", required = true, example = "ORD-001")
            @PathVariable String orderCode) {
        
        log.info("Quick order tracking request: {}", orderCode);
        
        // Call service for quick tracking
        PublicOrderTrackingResponse response = publicOrderService.quickTrackByCode(orderCode);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                "Tra cứu nhanh thành công.  Vui lòng nhập email để xem đầy đủ thông tin.", 
                response
            )
        );
    }
    
    /**
     * POST /api/v1/public/orders/validate-access
     * Kiểm tra mã đơn và email có khớp không
     */
    @PostMapping("/validate-access")
    @Operation(
        summary = "Kiểm tra quyền truy cập đơn hàng",
        description = "Kiểm tra mã đơn và email có khớp không trước khi tra cứu chi tiết"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateOrderAccess(
            @Valid @RequestBody TrackOrderRequest request) {
        
        log.info("Validating order access:  {}", request.getOrderCode());
        
        // Validate access
        boolean hasAccess = publicOrderService.validateOrderAccess(
            request.getOrderCode(), 
            request.getEmail()
        );
        
        String message = hasAccess 
            ? "Thông tin hợp lệ. Có thể tra cứu đơn hàng."
            : "Mã đơn hàng và email không khớp. Vui lòng kiểm tra lại. ";
        
        Map<String, Object> result = Map.of(
            "hasAccess", hasAccess,
            "orderCode", request.getOrderCode(),
            "suggestion", hasAccess 
                ? "Bấm 'Tra cứu' để xem chi tiết đơn hàng."
                : "Kiểm tra lại mã đơn hàng và email đã dùng khi đặt hàng."
        );
        
        return ResponseEntity. ok(
            ApiResponse.success(message, result)
        );
    }
    
    /**
     * GET /api/v1/public/orders/tracking-guide
     * Hướng dẫn tra cứu đơn hàng
     */
    @GetMapping("/tracking-guide")
    @Operation(
        summary = "Hướng dẫn tra cứu đơn hàng",
        description = "Lấy thông tin hướng dẫn cách tra cứu đơn hàng và các câu hỏi thường gặp"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTrackingGuide() {
        
        Map<String, Object> guide = Map.of(
            "title", "Hướng dẫn tra cứu đơn hàng",
            "description", "Nhập mã đơn hàng và email để tra cứu trạng thái đơn hàng của bạn",
            "steps", new String[]{
                "1. Nhập mã đơn hàng (VD: ORD-001, ORD-002... )",
                "2. Nhập email đã sử dụng khi đặt hàng",
                "3. Nhấn 'Tra cứu' để xem kết quả",
                "4. Xem trạng thái và thông tin chi tiết đơn hàng"
            },
            "notes", new String[]{
                "• Cả hai thông tin phải chính xác để có thể tra cứu",
                "• Mã đơn hàng được gửi qua email sau khi đặt hàng",
                "• Nếu không nhớ email, vui lòng liên hệ hỗ trợ"
            },
            "orderStatuses", Map.of(
                "PENDING", "Chờ xác nhận - Đơn hàng đang chờ cửa hàng xử lý",
                "CONFIRMED", "Đã xác nhận - Đơn hàng đã được xác nhận và chuẩn bị hàng",
                "SHIPPING", "Đang giao hàng - Đơn hàng đang được vận chuyển",
                "DELIVERED", "Đã giao hàng - Đơn hàng đã được giao thành công",
                "CANCELLED", "Đã hủy - Đơn hàng đã bị hủy"
            ),
            "support", Map.of(
                "email", "support@utephonehub.com",
                "hotline", "1900-XXX-XXX",
                "hours", "8:00 - 22:00 hàng ngày"
            ),
            "faq", new String[]{
                "Q:  Tại sao tôi không tra cứu được đơn hàng?",
                "A: Vui lòng kiểm tra lại mã đơn và email đã dùng khi đặt hàng.",
                "",
                "Q: Bao lâu sau khi đặt hàng thì có thể tra cứu?",
                "A: Ngay sau khi đặt hàng thành công, bạn đã có thể tra cứu.",
                "",
                "Q:  Tôi có thể hủy đơn hàng không?",
                "A:  Bạn có thể hủy đơn khi đơn đang ở trạng thái 'Chờ xác nhận'."
            }
        );
        
        return ResponseEntity.ok(
            ApiResponse.success("Lấy hướng dẫn thành công", guide)
        );
    }
    
    /**
     * GET /api/v1/public/orders/statistics
     * Thống kê tra cứu đơn hàng (cho admin)
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Thống kê tra cứu đơn hàng",
        description = "Lấy thống kê về tình hình đơn hàng (dành cho admin hoặc hiển thị công khai)"
    )
    public ResponseEntity<ApiResponse<Object>> getTrackingStatistics() {
        
        Object statistics = publicOrderService.getTrackingStatistics();
        
        return ResponseEntity.ok(
            ApiResponse.success("Lấy thống kê thành công", statistics)
        );
    }
}