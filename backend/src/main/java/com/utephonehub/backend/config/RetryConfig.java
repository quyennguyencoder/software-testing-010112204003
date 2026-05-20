package com.utephonehub.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // Enable Spring Retry for handling database deadlocks and optimistic locking conflicts
}
