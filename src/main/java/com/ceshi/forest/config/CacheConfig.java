package com.ceshi.forest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 配置 Caffeine 本地缓存 和 Redis 分布式缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 本地缓存管理器（一级缓存）
     */
    @Bean
    @Primary  // 默认使用
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 最大缓存数量
                .maximumSize(1000)
                // 写入后过期时间
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // 访问后过期时间
                .expireAfterAccess(5, TimeUnit.MINUTES)
                // 记录缓存统计
                .recordStats()
        );
        // 允许缓存 null 值，防止缓存穿透
        cacheManager.setAllowNullValues(true);
        return cacheManager;
    }

    /**
     * Redis 缓存管理器（二级缓存）
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 键序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 值序列化（JSON）
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                // 禁用缓存 null 值
                .disableCachingNullValues()
                // 默认过期时间 30 分钟
                .entryTtl(Duration.ofMinutes(30));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}