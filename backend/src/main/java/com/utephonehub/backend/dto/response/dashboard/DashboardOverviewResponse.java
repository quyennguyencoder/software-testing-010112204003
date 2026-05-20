package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewResponse {

    /**
     * Tổng doanh thu (VNĐ)
     */
    private BigDecimal totalRevenue;

    /**
     * Tổng số đơn hàng
     */
    private Long totalOrders;

    /**
     * Tổng số sản phẩm
     */
    private Long totalProducts;

    /**
     * Tổng số người dùng
     */
    private Long totalUsers;
}
