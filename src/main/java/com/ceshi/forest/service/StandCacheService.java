package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;

import java.util.List;

public interface StandCacheService {

    String KEY_PREFIX = "forest:stand:";

    StandDTO getStandById(Integer id);
    List<StandDTO> getAllStands();
    List<StatisticsDTO> getSpeciesStatistics();
    List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters);
    List<StandDTO> getHighValueStands(Double minVolumePerHa);
    void clearStandCache(Integer id);
    void clearAllStandCache();
    void refreshStand(Integer id);
}