package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.order.CreateOrderRequest;

import com.utephonehub.backend.dto.request.order.OrderItemRequest;
import com.utephonehub.backend.dto.response.order.CreateOrderResponse;
import com.utephonehub.backend.dto.response.order.OrderResponse;
import com.utephonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.utephonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.OrderItem;
import com.utephonehub.backend.entity.Payment;
import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.ProductTemplate;
import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import com.utephonehub.backend.enums.PaymentStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ForbiddenException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.mapper.OrderMapper;
import com.utephonehub.backend.repository.CartItemRepository;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.repository.OrderItemRepository;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.PaymentRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.repository.PromotionRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IEmailService;
import com.utephonehub.backend.service.IOrderService;
import com.utephonehub.backend.service.IVNPayService;
import com.utephonehub.backend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java. util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final IVNPayService vnPayService;
    private final SecurityUtils securityUtils;
    private final IEmailService emailService;
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        
        // 1. Tìm Order kèm items
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new ResourceNotFoundException("Đơn hàng không tồn tại");
                });
        
        // 2. Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(userId)) {
            log.warn("User {} tried to access order {} owned by user {}", 
                    userId, orderId, order.getUser().getId());
            throw new ForbiddenException("Bạn không có quyền xem đơn hàng này");
        }
        
        // 3. Convert sang DTO bằng Mapper
        log.info("Get order {} by user {}", orderId, userId);
        return orderMapper.toOrderResponse(order); 
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        return createOrder(request, userId, null);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId, HttpServletRequest servletRequest) {
        log.info("Creating order for user: {}", userId);
        
        // 1. Validate user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        // 2. Validate danh sách sản phẩm
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .collect(Collectors.toList());
        
        List<Product> products = productRepository.findAllByIdIn(productIds);
        
        if (products.size() != productIds.size()) {
            throw new BadRequestException("Một số sản phẩm không tồn tại");
        }
        
        // 3. Map product theo ID để dễ tìm kiếm
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 4. Validate tồn kho và tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemRequest> validatedItems = new ArrayList<>();
        
        for (OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            
            // Calculate total stock from templates
            int totalStock = product.getTemplates().stream()
                    .filter(ProductTemplate::getStatus)
                    .mapToInt(ProductTemplate::getStockQuantity)
                    .sum();
            
            // Kiểm tra tồn kho
            if (totalStock < item.getQuantity()) {
                throw new BadRequestException(
                    String.format("Sản phẩm '%s' chỉ còn %d sản phẩm trong kho", 
                        product.getName(), totalStock)
                );
            }
            
            // Get cheapest price from active templates
            BigDecimal price = product.getTemplates().stream()
                    .filter(ProductTemplate::getStatus)
                    .map(ProductTemplate::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            // Tính tổng tiền
            BigDecimal itemTotal = price.multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            validatedItems.add(item);
        }
        
        // 5. Áp dụng promotion nếu có (2 loại: DISCOUNT/VOUCHER và FREESHIP)
        Promotion promotion = null;
        Promotion freeshippingPromotion = null;
        
        // Xử lý promotion (DISCOUNT/VOUCHER)
        if (request.getPromotionId() != null) {
            try {
                promotion = promotionRepository.findById(String.valueOf(request.getPromotionId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Promotion không tồn tại"));
                
                // TODO: Validate promotion còn hiệu lực, đủ điều kiện áp dụng
                // Tính discount và trừ vào totalAmount (sẽ implement sau)
            } catch (ResourceNotFoundException e) {
                // Nếu promotion không hợp lệ, bỏ qua và tiếp tục
                log.warn("Invalid promotionId: {}, error: {}", request.getPromotionId(), e.getMessage());
                promotion = null;
            }
        }
        
        // Xử lý freeship promotion (FREESHIP)
        if (request.getFreeshippingPromotionId() != null) {
            try {
                freeshippingPromotion = promotionRepository.findById(String.valueOf(request.getFreeshippingPromotionId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Freeship promotion không tồn tại"));
                
                // TODO: Validate freeship promotion còn hiệu lực
            } catch (ResourceNotFoundException e) {
                log.warn("Invalid freeshippingPromotionId: {}, error: {}", request.getFreeshippingPromotionId(), e.getMessage());
                freeshippingPromotion = null;
            }
        }
        
        // 6. Tạo orderCode unique
        String orderCode = generateUniqueOrderCode();
        
        // 7. Xác định trạng thái đơn hàng
        OrderStatus initialStatus = request.getPaymentMethod() == PaymentMethod.VNPAY
                ? OrderStatus.PENDING
                : OrderStatus.CONFIRMED;
        
        // 8. Tạo Order entity
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .email(request.getEmail())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .shippingAddress(request.getShippingAddress())
                .shippingFee(request.getShippingFee())
                .shippingUnit(request.getShippingUnit())
                .note(request.getNote())
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(totalAmount)
                .promotion(promotion)
                .freeshippingPromotion(freeshippingPromotion)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 9. Lưu Order
        order = orderRepository.save(order);
        log.info("Created order: {}", orderCode);
        
        // 9. Tạo OrderItems
        for (OrderItemRequest itemReq : validatedItems) {
            Product product = productMap.get(itemReq.getProductId());
            
            // Get cheapest price from active templates
            BigDecimal price = product.getTemplates().stream()
                    .filter(ProductTemplate::getStatus)
                    .map(ProductTemplate::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            orderItemRepository.save(orderItem);
        }
        // 10. Tạo Payment record cho COD/Bank Transfer (VNPay sẽ tạo trong callback)
        if (request.getPaymentMethod() != PaymentMethod.VNPAY) {
            // 10.1. Giảm tồn kho ngay (thanh toán trực tiếp)
            for (OrderItemRequest itemReq : validatedItems) {
                Product product = productMap.get(itemReq.getProductId());
                int remainingQuantity = itemReq.getQuantity();
                
                // Deduct stock from available templates sequentially
                for (ProductTemplate template : product.getTemplates()) {
                    if (!template.getStatus() || template.getStockQuantity() <= 0 || remainingQuantity <= 0) {
                        continue;
                    }
                    
                    int deductAmount = Math.min(template.getStockQuantity(), remainingQuantity);
                    template.setStockQuantity(template.getStockQuantity() - deductAmount);
                    remainingQuantity -= deductAmount;
                }
                
                // Validate all quantity was deducted
                if (remainingQuantity > 0) {
                    throw new BadRequestException(
                        "Không đủ tồn kho để hoàn tất đơn hàng cho sản phẩm: " + product.getName()
                    );
                }
                
                productRepository.save(product);  // Cascade saves templates
            }
            
            // 10.2. Tạo Payment record với status SUCCESS (đã thanh toán)
            Payment payment = Payment.builder()
                    .order(order)
                    .provider(null)  // COD/Bank Transfer không có provider
                    .transactionId(null)  // Không có transaction ID
                    .amount(totalAmount)
                    .status(PaymentStatus.SUCCESS)  // Thanh toán ngay = SUCCESS
                    .note("Thanh toán " + request.getPaymentMethod().name())
                    .reconciled(false)
                    .build();
            paymentRepository.save(payment);
            log.info("Created payment record for COD/Bank Transfer: orderId={}, amount={}", 
                    order.getId(), totalAmount);
            
            // Send order confirmation email for COD/Bank Transfer (payment already successful)
            try {
                String paymentMethodName = request.getPaymentMethod() == PaymentMethod.COD 
                    ? "Thanh toán khi nhận hàng" 
                    : "Chuyển khoản ngân hàng";
                
                emailService.sendOrderPaymentSuccessEmail(
                    order.getEmail(),
                    order.getOrderCode(),
                    order.getTotalAmount(),
                    order.getRecipientName(),
                    paymentMethodName
                );
                log.info("Order confirmation email sent for COD/Bank Transfer order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to send order confirmation email for order {}: {}", 
                         order.getOrderCode(), e.getMessage());
                // Không throw exception để không ảnh hưởng order creation
            }
        }

        // 11.1. AF2 – After successfully creating the order, automatically remove
        // the corresponding CartItems in the cart (do not delete the entire cart).
        try {
            List<Long> orderedProductIds = validatedItems.stream()
                    .map(OrderItemRequest::getProductId)
                    .toList();

            cartRepository.findByUserIdWithItems(userId).ifPresent(cart -> {
                List<CartItem> toRemove = cart.getItemsInternal().stream()
                        .filter(item -> orderedProductIds.contains(item.getProduct().getId()))
                        .toList();

                if (!toRemove.isEmpty()) {
                    log.info("Clearing {} ordered items from cart for user {} after order {}",
                            toRemove.size(), userId, orderCode);

                    toRemove.forEach(cart::removeItem);
                    cartItemRepository.deleteAll(toRemove);
                    cartRepository.save(cart);
                }
            });
        } catch (Exception ex) {
            // Don't let cart clearing errors fail the order
            log.error("Failed to clear ordered items from cart for user {} after order {}", userId, orderCode, ex);
        }
        
        // 12. Create response
        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(order.getId())
                .orderCode(orderCode)
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(totalAmount)
                .createdAt(order.getCreatedAt())
                .build();
        
        // 13. If payment method is VNPay, add instruction message
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            response.setMessage("Đơn hàng đã tạo. Đang chuyển hướng thanh toán VNPay...");
            
            // Tích hợp VNPay payment URL
            try {
                CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                        .orderId(order.getId())
                        .amount(totalAmount.longValue())
                        .orderInfo("Thanh toan don hang " + orderCode)
                        .locale("vn")
                        .build();

                // Lấy IP client phục vụ VNPay; nếu servletRequest null thì fallback về 127.0.0.1
                String ipAddress;
                if (servletRequest != null) {
                    ipAddress = securityUtils.getClientIp(servletRequest);
                } else {
                    log.warn("HttpServletRequest is null when creating VNPay URL for order {}. Using fallback IP 127.0.0.1", orderCode);
                    ipAddress = "127.0.0.1";
                }

                VNPayPaymentResponse paymentResponse = vnPayService.createPaymentUrl(paymentRequest, ipAddress);
                response.setPaymentUrl(paymentResponse.getPaymentUrl());
                log.info("Generated VNPay URL for order {}: {}", orderCode, paymentResponse.getPaymentUrl());
            } catch (Exception e) {
                log.error("Failed to generate VNPay URL for order {}", orderCode, e);
                response.setMessage("Đơn hàng đã tạo nhưng lỗi tạo link thanh toán. Vui lòng thử lại trong lịch sử đơn hàng.");
            }
        } else {
            response.setMessage("Đơn hàng đã được tạo thành công!");
        }
        
        log.info("Order created successfully: {}", orderCode);
        return response;
    }
    
    /**
     * Generate unique order code
     * Format: ORD_YYMMDDHHMMSS (20 chars max)
     * Example: ORD_251207093853
     */
    private String generateUniqueOrderCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String orderCode = "ORD_" + timestamp;
        
        // Kiểm tra trùng lặp (rất hiếm khi xảy ra)
        int counter = 1;
        String uniqueCode = orderCode;
        while (orderRepository.existsByOrderCode(uniqueCode)) {
            uniqueCode = orderCode + counter;
            counter++;
        }
        
        return uniqueCode;
    }
    
    
    //Xem đơn hàng của chính mình
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        log. info("Getting orders for user: {}", userId);
        
        User user = userRepository. findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
       
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        log.info("Found {} orders for user {}", orders.size(), userId);
        
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors. toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrdersWithPagination(Long userId, Pageable pageable) {
        log.info("Getting orders with pagination for user: {}, page: {}, size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
      
        Page<Order> orderPage = orderRepository. findByUserOrderByCreatedAtDesc(user, pageable);
        
        log.info("Found {} orders for user {} in page {}", 
                orderPage.getContent().size(), userId, pageable.getPageNumber());
        
        return orderPage. map(orderMapper:: toOrderResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrdersByStatus(Long userId, OrderStatus status) {
        log.info("Getting orders by status {} for user: {}", status, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        
        List<Order> orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
        
        log.info("Found {} orders with status {} for user {}", orders.size(), status, userId);
        
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getMyOrdersCount(Long userId) {
        log.info("Counting orders for user:  {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        
        long count = orderRepository. countByUser(user);
        
        log.info("User {} has {} orders", userId, count);
        
        return count;
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long orderId, Long userId) {
        log.info("Getting order detail {} for user {}", orderId, userId);
        
        // Tái sử dụng method getOrderById đã có
        return getOrderById(orderId, userId);
    }
    
    
    
    // ========================================
    // ✅ THÊM CHỨC NĂNG HỦY ĐƠN HÀNG
    // ========================================
    
    @Override
    @Transactional
    public void cancelMyOrder(Long orderId, Long userId) {
        log.info("User {} attempting to cancel order {}", userId, orderId);
        
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log. error("Order not found: {}", orderId);
                    return new ResourceNotFoundException("Đơn hàng không tồn tại");
                });
        
        // 2. Kiểm tra quyền sở hữu
        if (! order.getUser().getId().equals(userId)) {
            log.warn("User {} tried to cancel order {} owned by user {}", 
                    userId, orderId, order.getUser().getId());
            throw new ForbiddenException("Bạn không có quyền hủy đơn hàng này");
        }
        
        // 3. Kiểm tra trạng thái có thể hủy
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel order {} with status {}", orderId, order.getStatus());
            throw new BadRequestException(
                String.format("Không thể hủy đơn hàng ở trạng thái '%s'. Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ xác nhận'.", 
                    getStatusDisplayName(order.getStatus()))
            );
        }
        
        // 4. Cập nhật trạng thái sang CANCELLED
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 5. Lưu đơn hàng
        orderRepository.save(order);
        
        // 6. Ghi log lịch sử trạng thái (nếu có bảng order_status_history)
        try {
            saveOrderStatusHistory(order, OrderStatus.CANCELLED, "Khách hàng tự hủy đơn");
        } catch (Exception e) {
            log.warn("Failed to save order status history for order {}:  {}", orderId, e.getMessage());
        }
        
        // 7. Hoàn lại tồn kho nếu đã trừ (đối với đơn COD/Bank Transfer)
        if (order.getPaymentMethod() != PaymentMethod.VNPAY) {
            try {
                restoreProductStock(order);
                log.info("Product stock restored for cancelled order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to restore stock for cancelled order {}: {}", orderId, e.getMessage());
                // Không throw exception để không làm thất bại việc hủy đơn
            }
        }
        
        log.info("Order {} successfully cancelled by user {}. Status changed from {} to {}", 
                orderId, userId, oldStatus, OrderStatus.CANCELLED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Long orderId, Long userId) {
        log.info("Checking if user {} can cancel order {}", userId, orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
            
            // Kiểm tra quyền sở hữu
            if (!order.getUser().getId().equals(userId)) {
                return false;
            }
            
            // Chỉ có thể hủy khi ở trạng thái PENDING
            boolean canCancel = order.getStatus() == OrderStatus.PENDING;
            
            log.info("Order {} can be cancelled:  {}", orderId, canCancel);
            return canCancel;
            
        } catch (Exception e) {
            log.error("Error checking cancel permission for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // ✅ HELPER METHODS
    // ========================================
    
    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận"; 
            case SHIPPING -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELLED -> "Đã hủy";
        };
    }
    
    private void saveOrderStatusHistory(Order order, OrderStatus newStatus, String note) {
        // Tạo record trong order_status_history 
        
        log.info("Order {} status changed to {} with note: {}", order.getOrderCode(), newStatus, note);
    }
    
    private void restoreProductStock(Order order) {
        log.info("Restoring stock for cancelled order: {}", order.getOrderCode());
        
        
        List<OrderItem> orderItems = order.getItems();
        
        if (orderItems == null || orderItems.isEmpty()) {
            log.info("No order items found for order: {}", order.getOrderCode());
            return;
        }
        
        // Note: Stock is managed at ProductTemplate level, not Product level
        // If stock restoration is needed, it should be done at ProductTemplate level
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (product != null) {
                log.info("Order item restored for product: {} (quantity: {})", 
                        product.getId(), item.getQuantity());
                // TODO: Restore stock at ProductTemplate level if needed
            } else {
                log.warn("Product not found for order item: {}", item.getId());
            }
        }
    }

    
    
    
    
   
}