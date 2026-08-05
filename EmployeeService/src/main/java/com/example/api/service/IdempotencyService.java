package com.example.api.service; // Match your exact package folder path

import com.example.api.exception.IdempotencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_PREFIX = "idempotency:";

    public IdempotencyService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks if a key has already run or is currently processing.
     */
    public void validateKey(String key) {
        String redisKey = REDIS_PREFIX + key;
        Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(5));

        if (Boolean.FALSE.equals(isAbsent)) {
            Object currentValue = redisTemplate.opsForValue().get(redisKey);
            if ("PROCESSING".equals(currentValue)) {
                log.warn("Idempotency match caught: Key [{}] is currently running.", key);
                throw new IdempotencyException("A matching request with this key is already running. Please wait.");
            } else {
                log.warn("Idempotency match caught: Key [{}] already finished running.", key);
                throw new IdempotencyException("Duplicate request blocked. This transaction has already been completed.");
            }
        }
    }

    /**
     * Caches the completed execution response into the Redis token key space.
     */
    public void saveResponse(String key, Object responseObject) {
        String redisKey = REDIS_PREFIX + key;
        // Native RedisTemplate handles object-to-JSON serialization using Spring Boot 4's managed engine
        redisTemplate.opsForValue().set(redisKey, responseObject, Duration.ofHours(1));
        log.info("Successfully cached result response for Idempotency Key [{}]", key);
    }

    /**
     * Retrieves the previously saved response object payload from Redis.
     */
    public Object getCachedResponse(String key) {
        Object val = redisTemplate.opsForValue().get(REDIS_PREFIX + key);
        if (val != null && !"PROCESSING".equals(val)) {
            return val;
        }
        return null;
    }

    /**
     * Clears out processing lock structures if an execution crashes.
     */
    public void removeKey(String key) {
        redisTemplate.delete(REDIS_PREFIX + key);
    }
}
