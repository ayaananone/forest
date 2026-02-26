package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;

import java.util.List;

/**
 * 林分缓存服务
 */
public interface StandCacheService {

    /**
     * 缓存键前缀
     */
    String KEY_PREFIX = "forest:stand:";

    /**
     * 获取林分详情（带缓存）
     */
    StandDTO getStandById(Integer id);

    /**
     * 获取所有林分（带缓存）
     */
    List<StandDTO> getAllStands();

    /**
     * 获取树种统计（带缓存）
     */
    List<StatisticsDTO> getSpeciesStatistics();

    /**
     * 获取附近林分（短时间缓存）
     */
    List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters);

    /**
     * 获取高价值林分（带缓存）
     */
    List<StandDTO> getHighValueStands(Double minVolumePerHa);

    /**
     * 清除林分缓存
     */
    void clearStandCache(Integer id);

    /**
     * 清除所有林分缓存
     */
    void clearAllStandCache();
}