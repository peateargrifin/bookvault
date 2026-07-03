package com.bookvault.bookvault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitConfig {
    /**
     * Number of allowed requests per minute for unauthenticated users (per IP).
     */
    private int requestsPerMinute;

    /**
     * Number of allowed requests per minute for authenticated users.
     */
    private int authenticatedRequestsPerMinute;
}
