package com.bookvault.bookvault.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

// 📘 CONCEPT: Video 13 - Manual cache operations
// For fine-grained control beyond @Cacheable annotations
// Used for: rate limiting counters, session storage, custom TTLs
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 📘 CONCEPT: Video 13 - Cache-aside (lazy caching) pattern
    // Check cache → hit? return. Miss? fetch from DB → store → return
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("CACHE_HIT key={}", key);
            } else {
                log.debug("CACHE_MISS key={}", key);
            }
            return value;
        } catch (Exception e) {
            // 📘 CONCEPT: Video 16 - Graceful degradation
            // If Redis is down → log warning, fall through to DB
            // Never let cache failure break the actual request
            // 🏢 PRODUCT: cache is optimization, not requirement
            //             app must work without cache (just slower)
            log.warn("CACHE_GET_FAILED key={} error={}", key, e.getMessage());
            return null;
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("CACHE_SET key={} ttl={}s", key, ttl.getSeconds());
        } catch (Exception e) {
            log.warn("CACHE_SET_FAILED key={} error={}", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("CACHE_DELETE key={}", key);
        } catch (Exception e) {
            log.warn("CACHE_DELETE_FAILED key={} error={}", key, e.getMessage());
        }
    }

    // 📘 CONCEPT: Video 13 - Pattern-based cache invalidation
    // When a book is updated → invalidate ALL book listing caches
    // because any page might now be stale
    // 🟡 NOVICE: only invalidate the specific page → other pages show stale data
    // 🏢 PRODUCT: invalidate all related cache keys on write
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("CACHE_DELETE_PATTERN pattern={} count={}",
                        pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("CACHE_DELETE_PATTERN_FAILED pattern={} error={}",
                    pattern, e.getMessage());
        }
    }

    // 📘 CONCEPT: Video 13 + Video 14 - Rate limiting counter
    // Increment and return new count, set TTL on first increment
    // Used by RateLimitFilter for per-IP request counting
    public long incrementWithTtl(String key, Duration ttl) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                // First increment — set expiry for the time window
                redisTemplate.expire(key, ttl);
            }
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("CACHE_INCREMENT_FAILED key={} error={}",
                    key, e.getMessage());
            // 📘 CONCEPT: Video 16 - Fail open for rate limiting
            // If Redis down → allow request (don't block legitimate users)
            // Accept small DDoS risk over blocking real users
            return 0;
        }
    }
}
