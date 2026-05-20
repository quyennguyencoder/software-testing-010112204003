package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.dashboard.DashboardOverviewResponse;
import com.utephonehub.backend.dto.response.dashboard.LowStockProductResponse;
import com.utephonehub.backend.dto.response.dashboard.OrderStatusChartResponse;
import com.utephonehub.backend.dto.response.dashboard.RecentOrderResponse;
import com.utephonehub.backend.dto.response.dashboard.RevenueChartResponse;
import com.utephonehub.backend.dto.response.dashboard.TopProductResponse;
import com.utephonehub.backend.dto.response.dashboard.UserRegistrationChartResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.ProductTemplate;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.DashboardPeriod;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.RegistrationPeriod;
import com.utephonehub.backend.repository.OrderItemRepository;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public DashboardOverviewResponse getOverview() {
        log.info("Fetching dashboard overview statistics");

        // Calculate total revenue from DELIVERED orders (completed orders)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueByStatus(OrderStatus.DELIVERED);
        
        // Count total orders
        long totalOrders = orderRepository.count();
        
        // Count total products
        long totalProducts = productRepository.count();
        
        // Count total users
        long totalUsers = userRepository.count();

        log.info("Dashboard overview - Revenue: {}, Orders: {}, Products: {}, Users: {}", 
                totalRevenue, totalOrders, totalProducts, totalUsers);

        return DashboardOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .build();
    }

    @Override
    public RevenueChartResponse getRevenueChart(DashboardPeriod period) {
        log.info("Fetching revenue chart data for period: {}", period);

        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period.getDays() - 1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        log.debug("Date range: {} to {}", startDateTime, endDateTime);

        // Fetch orders in date range with DELIVERED status
        List<Order> orders = orderRepository.findByCreatedAtBetweenAndStatus(
                startDateTime, endDateTime, OrderStatus.DELIVERED);

        log.debug("Found {} delivered orders in period", orders.size());

        // Group orders by date and sum revenue
        Map<LocalDate, BigDecimal> revenueByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getTotalAmount,
                                BigDecimal::add
                        )
                ));

        // Build chart data with all dates (fill missing dates with zero)
        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            labels.add(currentDate.format(dateFormatter));
            values.add(revenueByDate.getOrDefault(currentDate, BigDecimal.ZERO));
            currentDate = currentDate.plusDays(1);
        }

        // Calculate total revenue
        BigDecimal totalRevenue = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average revenue per day
        BigDecimal averagePerDay = totalRevenue.divide(
                BigDecimal.valueOf(period.getDays()), 
                2, 
                RoundingMode.HALF_UP
        );

        log.info("Revenue chart - Total: {}, Average/Day: {}, Period: {}", 
                totalRevenue, averagePerDay, period);

        return RevenueChartResponse.builder()
                .labels(labels)
                .values(values)
                .total(totalRevenue)
                .averagePerDay(averagePerDay)
                .period(period.name())
                .build();
    }

    @Override
    public OrderStatusChartResponse getOrderStatusChart() {
        log.info("Fetching order status distribution chart data");

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        List<Double> percentages = new ArrayList<>();
        
        // Count total orders
        long totalOrders = orderRepository.count();
        
        log.debug("Total orders: {}", totalOrders);

        // Vietnamese labels for each OrderStatus
        Map<OrderStatus, String> statusLabels = Map.of(
                OrderStatus.PENDING, "Chờ xác nhận",
                OrderStatus.CONFIRMED, "Đã xác nhận",
                OrderStatus.SHIPPING, "Đang giao hàng",
                OrderStatus.DELIVERED, "Đã giao hàng",
                OrderStatus.CANCELLED, "Đã hủy"
        );

        // Count orders for each status
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByStatus(status);
            double percentage = totalOrders > 0 
                    ? (count * 100.0 / totalOrders) 
                    : 0.0;
            
            // Round to 2 decimal places
            percentage = Math.round(percentage * 100.0) / 100.0;
            
            labels.add(statusLabels.get(status));
            values.add(count);
            percentages.add(percentage);
            
            log.debug("Status: {} - Count: {}, Percentage: {}%", status, count, percentage);
        }

        log.info("Order status chart - Total orders: {}", totalOrders);

        return OrderStatusChartResponse.builder()
                .labels(labels)
                .values(values)
                .percentages(percentages)
                .totalOrders(totalOrders)
                .build();
    }

    @Override
    public UserRegistrationChartResponse getUserRegistrationChart(RegistrationPeriod period) {
        log.info("Fetching user registration chart data for period: {}", period);

        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(period.getDays() - 1);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        log.debug("Date range: {} to {}", startDateTime, endDateTime);

        // Fetch users registered in date range
        List<User> users = userRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        log.debug("Found {} new users in period", users.size());

        // Group users by registration date and count
        Map<LocalDate, Long> usersByDate = users.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreatedAt().toLocalDate(),
                        Collectors.counting()
                ));

        // Build chart data with all dates (fill missing dates with zero)
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            labels.add(currentDate.format(dateFormatter));
            values.add(usersByDate.getOrDefault(currentDate, 0L));
            currentDate = currentDate.plusDays(1);
        }

        // Calculate total new users
        long total = values.stream()
                .mapToLong(Long::longValue)
                .sum();

        log.info("User registration chart - Total new users: {}, Period: {}", total, period);

        return UserRegistrationChartResponse.builder()
                .labels(labels)
                .values(values)
                .total(total)
                .period(period.name())
                .build();
    }

    @Override
    public List<TopProductResponse> getTopProducts(int limit) {
        log.info("Fetching top {} selling products", limit);

        // Validate and cap limit to avoid fetching excessive records
        if (limit < 1) {
            log.warn("Limit {} is less than 1, setting to default 10", limit);
            limit = 10;
        }

        Pageable pageable = PageRequest.of(0, limit);

        // Get top selling products from OrderItem aggregation
        List<Object[]> results = orderItemRepository.findTopSellingProducts(pageable);

        // Convert to DTO list
        List<TopProductResponse> topProducts = results.stream()
                .map(row -> {
                    Product product = (Product) row[0];
                    Long totalSold = ((Number) row[1]).longValue();
                    BigDecimal revenue = (BigDecimal) row[2];

                    return TopProductResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .imageUrl(product.getThumbnailUrl())
                            .totalSold(totalSold)
                            .revenue(revenue)
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Found {} top selling products", topProducts.size());

        return topProducts;
    }

    @Override
    public List<RecentOrderResponse> getRecentOrders(int limit) {
        log.info("Fetching {} recent orders", limit);

        // Validate and cap limit at 20
        if (limit > 20) {
            log.warn("Limit {} exceeds maximum 20, capping to 20", limit);
            limit = 20;
        }
        if (limit < 1) {
            log.warn("Limit {} is less than 1, setting to default 10", limit);
            limit = 10;
        }

        // Create Pageable for limiting results
        Pageable pageable = PageRequest.of(0, limit);

        // Fetch recent orders sorted by createdAt DESC
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);

        // Vietnamese status labels mapping
        Map<OrderStatus, String> statusLabels = Map.of(
                OrderStatus.PENDING, "Chờ xác nhận",
                OrderStatus.CONFIRMED, "Đã xác nhận",
                OrderStatus.SHIPPING, "Đang giao hàng",
                OrderStatus.DELIVERED, "Đã giao hàng",
                OrderStatus.CANCELLED, "Đã hủy"
        );

        // Convert to DTO list
        List<RecentOrderResponse> recentOrders = orders.stream()
                .map(order -> RecentOrderResponse.builder()
                        .orderId(order.getId())
                        .customerName(order.getRecipientName())
                        .customerEmail(order.getEmail())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus())
                        .statusLabel(statusLabels.get(order.getStatus()))
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        log.info("Found {} recent orders", recentOrders.size());

        return recentOrders;
    }

    @Override
    public List<LowStockProductResponse> getLowStockProducts(int threshold) {
        log.info("Fetching low stock products with threshold: {}", threshold);

        // Validate threshold (minimum 0, reasonable default 10)
        if (threshold < 0) {
            log.warn("Threshold {} is negative, setting to 0", threshold);
            threshold = 0;
        }

        // Fetch products with stock <= threshold, only active products, sorted by stock ASC
        List<Product> lowStockProducts = productRepository
                .findByStockQuantityLessThanEqualAndStatusTrueOrderByStockQuantityAsc(threshold);

        // Convert to DTO list
        List<LowStockProductResponse> response = lowStockProducts.stream()
                .map(product -> {
                    int totalStock = product.getTemplates().stream()
                            .filter(ProductTemplate::getStatus)
                            .mapToInt(ProductTemplate::getStockQuantity)
                            .sum();
                    return LowStockProductResponse.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .imageUrl(product.getThumbnailUrl())
                            .stockQuantity(totalStock)
                            .categoryName(product.getCategory() != null ? product.getCategory().getName() : "N/A")
                            .brandName(product.getBrand() != null ? product.getBrand().getName() : "N/A")
                            .status(product.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        log.info("Found {} low stock products (threshold: {})", response.size(), threshold);

        return response;
    }
}
