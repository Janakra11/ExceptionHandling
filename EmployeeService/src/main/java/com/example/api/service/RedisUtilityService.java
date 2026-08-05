package com.example.api.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RedisUtilityService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtilityService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long incrementMetricCounter(String metricName) {
        String targetKey = "metrics:counters:" + metricName;
        return redisTemplate.opsForValue().increment(targetKey);
    }

    public boolean isRateLimitPermissible(String clientIp, int limitMax) {
        String throttlingKey = "security:rate:" + clientIp;
        Long totalCurrentHits = redisTemplate.opsForValue().increment(throttlingKey);

        if (totalCurrentHits != null && totalCurrentHits == 1) {
            redisTemplate.expire(throttlingKey, Duration.ofMinutes(1));
        }

        return totalCurrentHits != null && totalCurrentHits <= limitMax;
    }
}
