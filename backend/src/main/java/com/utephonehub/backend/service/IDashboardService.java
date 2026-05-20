package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.response.dashboard.DashboardOverviewResponse;
import com.utephonehub.backend.dto.response.dashboard.LowStockProductResponse;
import com.utephonehub.backend.dto.response.dashboard.OrderStatusChartResponse;
import com.utephonehub.backend.dto.response.dashboard.RecentOrderResponse;
import com.utephonehub.backend.dto.response.dashboard.RevenueChartResponse;
import com.utephonehub.backend.dto.response.dashboard.TopProductResponse;
import com.utephonehub.backend.dto.response.dashboard.UserRegistrationChartResponse;
import com.utephonehub.backend.enums.DashboardPeriod;
import com.utephonehub.backend.enums.RegistrationPeriod;

import java.util.List;

/**
 * Interface for Dashboard Service operations
 */
public interface IDashboardService {

    /**
     * Get dashboard overview statistics
     * - Total revenue (from completed orders)
     * - Total orders count
     * - Total products count
     * - Total users count
     * 
     * @return DashboardOverviewResponse with all statistics
     */
    DashboardOverviewResponse getOverview();
    
    /**
     * Get revenue chart data by time period
     * - Daily revenue for selected period
     * - Total and average revenue
     * 
     * @param period Time period (SEVEN_DAYS, THIRTY_DAYS, THREE_MONTHS)
     * @return RevenueChartResponse with labels, values, total, average
     */
    RevenueChartResponse getRevenueChart(DashboardPeriod period);
    
    /**
     * Get order status distribution chart data
     * - Count of orders by each status
     * - Percentage distribution
     * 
     * @return OrderStatusChartResponse with labels, values, percentages, total
     */
    OrderStatusChartResponse getOrderStatusChart();
    
    /**
     * Get user registration chart data by time period
     * - Daily new user registrations for selected period
     * - Total new users in the period
     * 
     * @param period Time period (WEEKLY, MONTHLY)
     * @return UserRegistrationChartResponse with labels, values, total
     */
    UserRegistrationChartResponse getUserRegistrationChart(RegistrationPeriod period);
    
    /**
     * Get top selling products
     * - Products sorted by total quantity sold (from DELIVERED orders only)
     * - Includes product info, total sold quantity, and revenue
     * 
     * @param limit Number of top products to return (e.g., 5 for Top 5)
     * @return List of TopProductResponse
     */
    List<TopProductResponse> getTopProducts(int limit);
    
    /**
     * Get recent orders
     * - Orders sorted by creation date (newest first)
     * - Includes customer info, order amount, status
     * 
     * @param limit Number of recent orders to return (default: 10, max: 20)
     * @return List of RecentOrderResponse
     */
    List<RecentOrderResponse> getRecentOrders(int limit);
    
    /**
     * Get low stock products (products below threshold)
     * - Products with stock quantity <= threshold
     * - Only active products (status = true)
     * - Sorted by stock quantity ascending (lowest first)
     * 
     * @param threshold Stock quantity threshold for warning
     * @return List of LowStockProductResponse
     */
    List<LowStockProductResponse> getLowStockProducts(int threshold);
}
