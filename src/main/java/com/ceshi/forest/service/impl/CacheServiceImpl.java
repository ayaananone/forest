package com.ceshi.forest.service.impl;

import com.ceshi.forest.service.CacheService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存实现（Caffeine + Redis）
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    // 本地缓存（Caffeine）
    private Cache<String, Object> localCache;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        localCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .recordStats()
                .build();
        logger.info("本地缓存初始化完成");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        // 1. 先查本地缓存
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            logger.debug("本地缓存命中: {}", key);
            return (T) value;
        }

        // 2. 再查 Redis
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            logger.debug("Redis缓存命中: {}", key);
            // 回填本地缓存
            localCache.put(key, value);
            return (T) value;
        }

        logger.debug("缓存未命中: {}", key);
        return null;
    }

    @Override
    public <T> T getOrLoad(String key, Class<T> clazz, CacheLoader<T> loader) {
        T value = get(key, clazz);
        if (value != null) {
            return value;
        }

        // 加锁防止缓存击穿（简单实现，生产环境可用 Redisson）
        synchronized (key.intern()) {
            // 双重检查
            value = get(key, clazz);
            if (value != null) {
                return value;
            }

            // 加载数据
            logger.info("从数据库加载数据: {}", key);
            value = loader.load();

            if (value != null) {
                // 默认缓存：本地10分钟，Redis30分钟
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

        // 写入本地缓存
        if (localExpireSeconds > 0) {
            localCache.put(key, value);
            logger.debug("写入本地缓存: {}", key);
        }

        // 写入 Redis
        if (redisExpireSeconds > 0) {
            redisTemplate.opsForValue().set(key, value, redisExpireSeconds, TimeUnit.SECONDS);
            logger.debug("写入Redis缓存: {}", key);
        }
    }

    @Override
    public void delete(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
        logger.info("删除缓存: {}", key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        // 删除本地缓存（遍历，性能较差，适合少量数据）
        localCache.asMap().keySet().removeIf(key -> key.matches(pattern.replace("*", ".*")));

        // 删除 Redis 缓存
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            logger.info("批量删除缓存，模式: {}, 数量: {}", pattern, keys.size());
        }
    }

    /**
     * 获取本地缓存统计
     */
    public String getLocalCacheStats() {
        return localCache.stats().toString();
    }
}