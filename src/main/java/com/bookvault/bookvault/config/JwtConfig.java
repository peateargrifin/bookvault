package com.bookvault.bookvault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    /**
     * The secret key used to sign the JWT tokens.
     */
    private String secret;

    /**
     * The expiration time of the JWT token in milliseconds.
     */
    private long expirationMs;
}
