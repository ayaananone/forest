package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;

import java.util.List;

/**
 * 林分服务接口（扩展）
 */
public interface ForestStandService {

    // 原有方法
    List<StandDTO> getAllStands();
    StandDTO getStandById(Integer id);
    List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters);
    List<StandDTO> getHighValueStands(Double minVolumePerHa);
    List<StatisticsDTO> getSpeciesStatistics();

    // 新增CRUD方法
    StandDTO createStand(StandDTO dto, String operator);
    StandDTO updateStand(StandDTO dto, String operator);
    void deleteStand(Integer id, String operator);
    List<StandDTO> batchUpdateStands(List<StandDTO> dtoList, String operator);
    void batchDeleteStands(List<Integer> ids, String operator);
    boolean checkStandCodeExists(String xiaoBanCode, Integer excludeId);
    StandDTO getStandByCode(String xiaoBanCode);
}