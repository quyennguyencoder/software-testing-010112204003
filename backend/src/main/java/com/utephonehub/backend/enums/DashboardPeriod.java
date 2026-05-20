package com.utephonehub.backend.enums;

/**
 * Dashboard time period enumeration for charts
 */
public enum DashboardPeriod {
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
    THREE_MONTHS(90);

    private final int days;

    DashboardPeriod(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
