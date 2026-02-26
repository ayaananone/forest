package com.ceshi.forest.service;

/**
 * 通用缓存服务接口
 * 支持本地缓存 + Redis 二级缓存
 */
public interface CacheService {

    /**
     * 获取缓存
     * @param key 缓存键
     * @param clazz 值类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取缓存，不存在则加载
     * @param key 缓存键
     * @param clazz 值类型
     * @param loader 加载函数
     * @return 缓存值
     */
    <T> T getOrLoad(String key, Class<T> clazz, CacheLoader<T> loader);

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param localExpireSeconds 本地缓存过期时间（秒），0表示不缓存到本地
     * @param redisExpireSeconds Redis过期时间（秒），0表示不缓存到Redis
     */
    void set(String key, Object value, long localExpireSeconds, long redisExpireSeconds);

    /**
     * 删除缓存
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存（模式匹配）
     * @param pattern 匹配模式，如 "forest:stand:*"
     */
    void deleteByPattern(String pattern);

    /**
     * 缓存加载函数接口
     */
    @FunctionalInterface
    interface CacheLoader<T> {
        T load();
    }
}