package com.smol.shortener.service.impl;

import com.smol.shortener.config.AppProperties;
import com.smol.shortener.exception.SmolExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public void checkRateLimit(String ipAddress) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            int maxRequests = appProperties.getRateLimit().getMaxRequests();
            int windowSeconds = appProperties.getRateLimit().getWindowSeconds();

            Long count = redisTemplate.opsForValue().increment(key);

            if (count == null) {
                log.warn("Redis returned null for INCR on key {}.", key);
                return;
            }

            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            if (count > maxRequests) {
                log.warn("Rate limit exceeded for IP: {} ({}/{})", ipAddress, count, maxRequests);
                throw new SmolExceptions.RateLimitExceededException(ipAddress);
            }

        } catch (SmolExceptions.RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Rate limit check failed for IP {}, allowing request: {}", ipAddress, e.getMessage());
        }
    }

    public long getCurrentCount(String ipAddress) {
        try {
            String key = RATE_LIMIT_PREFIX + ipAddress;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}