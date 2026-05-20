
package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.order.TrackOrderRequest;
import com.utephonehub.backend.dto.response.order.PublicOrderTrackingResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com. utephonehub.backend. repository.OrderRepository;
import com. utephonehub.backend. service.IPublicOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicOrderServiceImpl implements IPublicOrderService {
    
    private final OrderRepository orderRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PublicOrderTrackingResponse trackOrder(TrackOrderRequest request) {
        log.info("Public tracking order:  {} with email: {}", 
                request.getOrderCode(), maskEmail(request.getEmail()));
        
        // 1. Validate input
        validateTrackingRequest(request);
        
        // 2. Find order by orderCode and email
        Order order = orderRepository. findByOrderCodeAndEmail(
                request.getOrderCode().trim().toUpperCase(), 
                request.getEmail().trim().toLowerCase()
            )
            .orElseThrow(() -> {
                log.warn("Order not found or email mismatch: {} - {}", 
                        request.getOrderCode(), maskEmail(request.getEmail()));
                return new ResourceNotFoundException(
                    "Không tìm thấy đơn hàng với mã và email đã nhập.  Vui lòng kiểm tra lại thông tin."
                );
            });
        
        log.info("Found order {} for public tracking", order.getOrderCode());
        
        // 3. Convert to public response DTO
        return PublicOrderTrackingResponse. fromEntity(order);
    }
    
    @Override
    @Transactional(readOnly = true)  
    public PublicOrderTrackingResponse quickTrackByCode(String orderCode) {
        log.info("Quick tracking order: {}", orderCode);
        
        // 1. Validate orderCode format
        if (orderCode == null || orderCode.trim().isEmpty()) {
            throw new BadRequestException("Mã đơn hàng không được để trống");
        }
        
        String cleanOrderCode = orderCode.trim().toUpperCase();
        
        // 2. Find order by orderCode only (less info)
        Order order = orderRepository.findByOrderCode(cleanOrderCode)
                .orElseThrow(() -> {
                    log.warn("Order not found for quick tracking: {}", cleanOrderCode);
                    return new ResourceNotFoundException("Không tìm thấy đơn hàng với mã:  " + cleanOrderCode);
                });
        
        log.info("Found order {} for quick tracking", order.getOrderCode());
        
        // 3. Convert to public response with limited info
        PublicOrderTrackingResponse response = PublicOrderTrackingResponse. fromEntity(order);
        
        // 4. Mask sensitive information for quick track
        response.setRecipientName("***");  // Hide recipient name
        response.setTotalAmount(null);     // Hide total amount
        response. setMaskedPhoneNumber("***"); // Hide phone
        response.setCustomerMessage("Để xem đầy đủ thông tin, vui lòng nhập email khi đặt hàng.");
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateOrderAccess(String orderCode, String email) {
        log.info("Validating order access: {} with email: {}", 
                orderCode, maskEmail(email));
        
        try {
            String cleanOrderCode = orderCode != null ? orderCode.trim().toUpperCase() : "";
            String cleanEmail = email != null ? email.trim().toLowerCase() : "";
            
            if (cleanOrderCode.isEmpty() || cleanEmail.isEmpty()) {
                return false;
            }
            
            boolean exists = orderRepository.existsByOrderCodeAndEmail(cleanOrderCode, cleanEmail);
            log.info("Order access validation result: {}", exists);
            return exists;
        } catch (Exception e) {
            log.error("Error validating order access: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object getTrackingStatistics() {
        log.info("Getting tracking statistics");
        
        try {
            long totalOrders = orderRepository.count();
            long deliveredOrders = orderRepository.countByStatus(com.utephonehub.backend.enums.OrderStatus. DELIVERED);
            long pendingOrders = orderRepository.countByStatus(com.utephonehub.backend.enums.OrderStatus.PENDING);
            long shippingOrders = orderRepository.countByStatus(com.utephonehub.backend.enums.OrderStatus.SHIPPING);
            
            Map<String, Object> stats = new HashMap<>();
            stats. put("totalOrders", totalOrders);
            stats.put("deliveredOrders", deliveredOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("shippingOrders", shippingOrders);
            stats.put("deliveryRate", totalOrders > 0 ? (double) deliveredOrders / totalOrders * 100 : 0);
            
            return stats;
        } catch (Exception e) {
            log.error("Error getting tracking statistics:  {}", e.getMessage());
            return Map.of("error", "Unable to fetch statistics");
        }
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private void validateTrackingRequest(TrackOrderRequest request) {
        if (request. getOrderCode() == null || request.getOrderCode().trim().isEmpty()) {
            throw new BadRequestException("Mã đơn hàng không được để trống");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống");
        }
        
        // Basic email format validation
        String email = request.getEmail().trim();
        if (!email.contains("@") || !email.contains(".")) {
            throw new BadRequestException("Email không đúng định dạng");
        }
    }
    
    // Helper method to mask email in logs
    private String maskEmail(String email) {
        if (email == null || ! email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "***@" + domain;
        }
        
        return username. substring(0, 2) + "***@" + domain;
    }
}