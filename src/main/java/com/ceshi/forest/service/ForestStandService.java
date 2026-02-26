package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;

import java.util.List;

/**
 * 林分服务接口
 */
public interface ForestStandService {

    /**
     * 获取所有林分
     */
    List<StandDTO> getAllStands();

    /**
     * 根据ID获取林分详情
     */
    StandDTO getStandById(Integer id);

    /**
     * 空间查询：获取指定半径内的林分
     */
    List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters);

    /**
     * 获取高价值林分
     */
    List<StandDTO> getHighValueStands(Double minVolumePerHa);

    /**
     * 获取树种统计
     */
    List<StatisticsDTO> getSpeciesStatistics();
}