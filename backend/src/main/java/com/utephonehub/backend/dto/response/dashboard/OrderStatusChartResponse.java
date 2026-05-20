package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusChartResponse {

    /**
     * Labels for chart (e.g., ["Chờ xác nhận", "Đã xác nhận", ...])
     */
    private List<String> labels;

    /**
     * Order counts for each status
     */
    private List<Long> values;

    /**
     * Percentage of each status (0-100)
     */
    private List<Double> percentages;

    /**
     * Total number of orders
     */
    private Long totalOrders;
}
