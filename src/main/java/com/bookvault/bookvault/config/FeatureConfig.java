package com.bookvault.bookvault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "features")
@Data
public class FeatureConfig {
    /**
     * Toggle for Elasticsearch integration.
     */
    private boolean elasticsearchEnabled;

    /**
     * Toggle for email notifications.
     */
    private boolean emailNotificationsEnabled;

    /**
     * Toggle for rate limiting functionality.
     */
    private boolean rateLimitingEnabled;
}
