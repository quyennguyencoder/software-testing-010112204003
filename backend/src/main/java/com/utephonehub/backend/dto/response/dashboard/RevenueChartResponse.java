package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueChartResponse {

    /**
     * Danh sách nhãn (labels) theo ngày - Format: yyyy-MM-dd
     * Ví dụ: ["2024-11-05", "2024-11-06", "2024-11-07"]
     */
    private List<String> labels;

    /**
     * Danh sách giá trị doanh thu tương ứng với từng ngày
     * Ví dụ: [5000000, 7200000, 6800000]
     */
    private List<BigDecimal> values;

    /**
     * Tổng doanh thu trong khoảng thời gian
     */
    private BigDecimal total;

    /**
     * Doanh thu trung bình mỗi ngày
     */
    private BigDecimal averagePerDay;

    /**
     * Khoảng thời gian được chọn
     */
    private String period;
}
