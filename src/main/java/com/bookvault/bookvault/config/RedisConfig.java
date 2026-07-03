package com.bookvault.bookvault.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// 📘 CONCEPT: Video 13 - Caching configuration
// 🟡 NOVICE: no caching → every GET /books hits PostgreSQL every time
// 🏢 PRODUCT: Redis sits in front of DB
//             1st request → DB query → store in Redis with TTL
//             Next 1000 requests → served from Redis in <1ms
//             At 1M requests/day → saves ~800ms per request vs Postgres
@Configuration
@EnableCaching
public class RedisConfig {

    // 📘 CONCEPT: Video 13 - Different TTLs for different data types
    // Books list changes rarely → cache 5 minutes
    // Individual book changes rarely → cache 10 minutes
    // Auth sessions → cache 30 minutes (handled by SessionService)
    public static final String BOOKS_CACHE      = "books";
    public static final String BOOK_CACHE       = "book";
    public static final String AUTHORS_CACHE    = "authors";

    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 📘 CONCEPT: Video 7 - Serialization for Redis storage
        // Store keys as plain strings, values as JSON
        // 🟡 NOVICE: use default Java serialization → brittle, unreadable
        // 🏢 PRODUCT: JSON serialization → human readable in Redis CLI,
        //             works across service restarts and code changes
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        // Default cache config
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(jsonSerializer()))
                // 📘 CONCEPT: Video 13 - Don't cache null values
                // Prevents "cache poisoning" with empty results
                .disableCachingNullValues();

        // 📘 CONCEPT: Video 13 - Per-cache TTL configuration
        // Different data has different staleness tolerance
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Book list — moderate TTL (changes when books are published)
        cacheConfigs.put(BOOKS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Single book — longer TTL (rarely changes once published)
        cacheConfigs.put(BOOK_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Authors list — longest TTL (changes least frequently)
        cacheConfigs.put(AUTHORS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
