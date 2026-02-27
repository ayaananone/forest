package com.ceshi.forest.service.impl;

import com.ceshi.forest.service.CacheService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存实现（Caffeine + Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private Cache<String, Object> localCache;

    @PostConstruct
    public void init() {
        localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .recordStats()
                .build();
        log.info("本地缓存初始化完成");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return (T) value;
        }

        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);
            return (T) value;
        }

        return null;
    }

    @Override
    public <T> T getOrLoad(String key, Class<T> clazz, CacheLoader<T> loader) {
        T value = get(key, clazz);
        if (value != null) {
            return value;
        }

        synchronized (key.intern()) {
            value = get(key, clazz);
            if (value != null) {
                return value;
            }

            value = loader.load();

            if (value != null) {
                set(key, value, 600, 1800);
            }

            return value;
        }
    }

    @Override
    public void set(String key, Object value, long localExpireSeconds, long redisExpireSeconds) {
        if (value == null) {
            return;
        }

        if (localExpireSeconds > 0) {
            localCache.put(key, value);
        }

        if (redisExpireSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, redisExpireSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void delete(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        localCache.asMap().keySet().removeIf(key -> key.matches(pattern.replace("*", ".*")));

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}