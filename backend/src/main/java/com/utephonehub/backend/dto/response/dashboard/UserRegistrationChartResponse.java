package com.utephonehub.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for User Registration Chart
 * Used for bar chart showing new user registrations over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationChartResponse {
    
    /**
     * Chart labels (dates in dd/MM format)
     */
    private List<String> labels;
    
    /**
     * Number of new users registered for each date
     */
    private List<Long> values;
    
    /**
     * Total number of new users in the period
     */
    private Long total;
    
    /**
     * Period type (WEEKLY or MONTHLY)
     */
    private String period;
}
