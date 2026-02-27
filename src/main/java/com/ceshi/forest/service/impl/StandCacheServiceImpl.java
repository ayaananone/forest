package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.CacheService;
import com.ceshi.forest.service.StandCacheService;
import com.ceshi.forest.service.ForestStandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 林分缓存服务实现
 */
@Service
public class StandCacheServiceImpl implements StandCacheService {

    private static final Logger logger = LoggerFactory.getLogger(StandCacheServiceImpl.class);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ForestStandService standService;

    @Override
    public StandDTO getStandById(Integer id) {
        String key = KEY_PREFIX + "id:" + id;

        return cacheService.getOrLoad(key, StandDTO.class, () -> {
            logger.info("缓存未命中，查询数据库，林分ID: {}", id);
            return standService.getStandById(id);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getAllStands() {
        String key = KEY_PREFIX + "all";

        return cacheService.getOrLoad(key, List.class, () -> {
            logger.info("缓存未命中，查询所有林分");
            return standService.getAllStands();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatisticsDTO> getSpeciesStatistics() {
        String key = KEY_PREFIX + "statistics:species";

        // 统计数据变化较少，缓存时间更长
        List<StatisticsDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            return result;
        }

        result = standService.getSpeciesStatistics();
        // 本地缓存1小时，Redis缓存2小时
        cacheService.set(key, result, 3600, 7200);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        // 空间查询结果变化大，只缓存短时间
        String key = String.format("%snearby:%.4f:%.4f:%d", KEY_PREFIX, lon, lat, radiusMeters);

        List<StandDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            return result;
        }

        result = standService.getNearbyStands(lon, lat, radiusMeters);
        // 只缓存5分钟
        cacheService.set(key, result, 300, 300);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        // 高价值林分查询，使用参数化缓存键
        String key = String.format("%shigh-value:%.2f", KEY_PREFIX, minVolumePerHa);

        List<StandDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            logger.debug("高价值林分缓存命中: minVolume={}", minVolumePerHa);
            return result;
        }

        logger.info("缓存未命中，查询高价值林分, minVolume={}", minVolumePerHa);
        result = standService.getHighValueStands(minVolumePerHa);
        // 缓存30分钟
        cacheService.set(key, result, 1800, 1800);
        return result;
    }

    @Override
    public void clearStandCache(Integer id) {
        cacheService.delete(KEY_PREFIX + "id:" + id);
        cacheService.delete(KEY_PREFIX + "all");
        // 清除相关的高价值缓存（使用模式匹配）
        cacheService.deleteByPattern(KEY_PREFIX + "high-value:*");
        logger.info("清除林分缓存: {}", id);
    }

    @Override
    public void clearAllStandCache() {
        cacheService.deleteByPattern(KEY_PREFIX + "*");
        logger.info("清除所有林分缓存");
    }
}