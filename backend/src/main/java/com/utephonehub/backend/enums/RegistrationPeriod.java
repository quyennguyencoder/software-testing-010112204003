package com.utephonehub.backend.enums;

import lombok.Getter;

/**
 * Enum for user registration chart time period selection
 */
@Getter
public enum RegistrationPeriod {
    WEEKLY(7),
    MONTHLY(30);

    private final int days;

    RegistrationPeriod(int days) {
        this.days = days;
    }
}
