package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.response.dashboard.DashboardOverviewResponse;
import com.utephonehub.backend.dto.response.dashboard.LowStockProductResponse;
import com.utephonehub.backend.dto.response.dashboard.OrderStatusChartResponse;
import com.utephonehub.backend.dto.response.dashboard.RecentOrderResponse;
import com.utephonehub.backend.dto.response.dashboard.RevenueChartResponse;
import com.utephonehub.backend.dto.response.dashboard.TopProductResponse;
import com.utephonehub.backend.dto.response.dashboard.UserRegistrationChartResponse;
import com.utephonehub.backend.enums.DashboardPeriod;
import com.utephonehub.backend.enums.RegistrationPeriod;
import com.utephonehub.backend.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Dashboard", description = "API thống kê và báo cáo cho Admin Dashboard")
@SecurityRequirement(name = "bearerAuth")
// Use hasRole to match authorities built as ROLE_ADMIN in JwtAuthenticationFilter
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(
            summary = "Lấy thống kê tổng quan",
            description = "Lấy 4 chỉ số chính: Tổng doanh thu (từ đơn hoàn thành), Tổng đơn hàng, Tổng sản phẩm, Tổng người dùng"
    )
    public ResponseEntity<ApiResponse<DashboardOverviewResponse>> getOverview() {
        log.info("Admin fetch dashboard overview");

        DashboardOverviewResponse overview = dashboardService.getOverview();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thống kê tổng quan thành công",
                overview
        ));
    }

    @GetMapping("/revenue-chart")
    @Operation(
            summary = "Lấy biểu đồ doanh thu theo thời gian",
            description = "Lấy dữ liệu biểu đồ doanh thu theo ngày cho khoảng thời gian được chọn. Mặc định: 30 ngày. Chỉ tính đơn hàng đã hoàn thành (DELIVERED)"
    )
    public ResponseEntity<ApiResponse<RevenueChartResponse>> getRevenueChart(
            @Parameter(description = "Khoảng thời gian (SEVEN_DAYS, THIRTY_DAYS, THREE_MONTHS). Mặc định: THIRTY_DAYS")
            @RequestParam(defaultValue = "THIRTY_DAYS") DashboardPeriod period
    ) {
        log.info("Admin fetch revenue chart for period: {}", period);

        RevenueChartResponse revenueChart = dashboardService.getRevenueChart(period);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy dữ liệu biểu đồ doanh thu thành công",
                revenueChart
        ));
    }

    @GetMapping("/order-status-chart")
    @Operation(
            summary = "Lấy biểu đồ phân bố đơn hàng theo trạng thái",
            description = "Lấy dữ liệu biểu đồ tròn (Pie chart) thể hiện số lượng và tỷ lệ phần trăm đơn hàng theo từng trạng thái (Chờ xác nhận, Đã xác nhận, Đang giao, Đã giao, Đã hủy)"
    )
    public ResponseEntity<ApiResponse<OrderStatusChartResponse>> getOrderStatusChart() {
        log.info("Admin fetch order status distribution chart");

        OrderStatusChartResponse statusChart = dashboardService.getOrderStatusChart();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy dữ liệu biểu đồ trạng thái đơn hàng thành công",
                statusChart
        ));
    }

    @GetMapping("/user-registration-chart")
    @Operation(
            summary = "Lấy biểu đồ người dùng đăng ký mới",
            description = "Lấy dữ liệu biểu đồ cột (Bar chart) thể hiện số lượng người dùng đăng ký mới theo ngày cho khoảng thời gian được chọn. Mặc định: MONTHLY (30 ngày)"
    )
    public ResponseEntity<ApiResponse<UserRegistrationChartResponse>> getUserRegistrationChart(
            @Parameter(description = "Khoảng thời gian (WEEKLY: 7 ngày, MONTHLY: 30 ngày). Mặc định: MONTHLY")
            @RequestParam(defaultValue = "MONTHLY") RegistrationPeriod period
    ) {
        log.info("Admin fetch user registration chart for period: {}", period);

        UserRegistrationChartResponse registrationChart = dashboardService.getUserRegistrationChart(period);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy dữ liệu biểu đồ đăng ký người dùng thành công",
                registrationChart
        ));
    }

    @GetMapping("/top-products")
    @Operation(
            summary = "Lấy Top sản phẩm bán chạy",
            description = "Lấy danh sách sản phẩm bán chạy nhất theo số lượng đã bán (chỉ tính từ đơn hàng DELIVERED). Sắp xếp từ cao đến thấp."
    )
    public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
            @Parameter(description = "Số lượng sản phẩm cần lấy (VD: 5 cho Top 5)")
            @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("Admin fetch top {} selling products", limit);

        List<TopProductResponse> topProducts = dashboardService.getTopProducts(limit);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách sản phẩm bán chạy thành công",
                topProducts
        ));
    }

    @GetMapping("/recent-orders")
    @Operation(
            summary = "Lấy danh sách đơn hàng gần đây",
            description = "Lấy danh sách đơn hàng mới nhất, sắp xếp theo thời gian tạo (mới nhất trước). Mặc định: 10 đơn, tối đa: 20 đơn."
    )
    public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> getRecentOrders(
            @Parameter(description = "Số lượng đơn hàng cần lấy (default: 10, max: 20)")
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Admin fetch {} recent orders", limit);

        List<RecentOrderResponse> recentOrders = dashboardService.getRecentOrders(limit);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng gần đây thành công",
                recentOrders
        ));
    }

    @GetMapping("/low-stock-products")
    @Operation(
            summary = "Lấy danh sách sản phẩm sắp hết hàng",
            description = "Lấy danh sách sản phẩm có số lượng tồn kho <= ngưỡng cảnh báo. Chỉ hiển thị sản phẩm đang hoạt động (status = true). Sắp xếp theo số lượng tồn kho tăng dần (ít nhất trước)."
    )
    public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts(
            @Parameter(description = "Ngưỡng cảnh báo tồn kho (VD: 10 = sản phẩm có <= 10 cái)")
            @RequestParam(defaultValue = "10") int threshold
    ) {
        log.info("Admin fetch low stock products with threshold: {}", threshold);

        List<LowStockProductResponse> lowStockProducts = dashboardService.getLowStockProducts(threshold);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách sản phẩm sắp hết hàng thành công",
                lowStockProducts
        ));
    }
}
