package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.CacheService;
import com.ceshi.forest.service.StandCacheService;
import com.ceshi.forest.service.ForestStandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 林分缓存服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandCacheServiceImpl implements StandCacheService {

    private final CacheService cacheService;
    private final ForestStandService standService;

    @Override
    public StandDTO getStandById(Integer id) {
        String key = KEY_PREFIX + "id:" + id;
        return cacheService.getOrLoad(key, StandDTO.class, () -> standService.getStandById(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getAllStands() {
        String key = KEY_PREFIX + "all";
        return cacheService.getOrLoad(key, List.class, () -> standService.getAllStands());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatisticsDTO> getSpeciesStatistics() {
        String key = KEY_PREFIX + "statistics:species";
        List<StatisticsDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            return result;
        }
        result = standService.getSpeciesStatistics();
        cacheService.set(key, result, 3600, 7200);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        String key = String.format("%snearby:%.4f:%.4f:%d", KEY_PREFIX, lon, lat, radiusMeters);
        List<StandDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            return result;
        }
        result = standService.getNearbyStands(lon, lat, radiusMeters);
        cacheService.set(key, result, 300, 300);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        String key = String.format("%shigh-value:%.2f", KEY_PREFIX, minVolumePerHa);
        List<StandDTO> result = cacheService.get(key, List.class);
        if (result != null) {
            return result;
        }
        result = standService.getHighValueStands(minVolumePerHa);
        cacheService.set(key, result, 1800, 1800);
        return result;
    }

    @Override
    public void clearStandCache(Integer id) {
        cacheService.delete(KEY_PREFIX + "id:" + id);
        cacheService.delete(KEY_PREFIX + "all");
        cacheService.deleteByPattern(KEY_PREFIX + "high-value:*");
    }

    @Override
    public void clearAllStandCache() {
        cacheService.deleteByPattern(KEY_PREFIX + "*");
    }
}