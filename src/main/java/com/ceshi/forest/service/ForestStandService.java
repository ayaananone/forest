package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import com.ceshi.forest.repository.ForestStandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForestStandService {

    @Autowired
    private ForestStandRepository standRepository;

    // 获取所有林分
    public List<StandDTO> getAllStands() {
        return standRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据ID获取林分
    public StandDTO getStandById(Integer id) {
        ForestStand stand = standRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("林分不存在"));
        return convertToDTO(stand);
    }

    // 空间查询：附近林分
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        return standRepository.findNearbyStands(lon, lat, radiusMeters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取高价值林分
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        return standRepository.findByVolumePerHaGreaterThanOrderByVolumePerHaDesc(minVolumePerHa)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取树种统计
    public List<StatisticsDTO> getSpeciesStatistics() {
        List<Object[]> results = standRepository.getStatisticsBySpecies();
        List<StatisticsDTO> stats = new ArrayList<>();

        for (Object[] row : results) {
            StatisticsDTO dto = new StatisticsDTO();
            dto.setSpecies((String) row[0]);
            dto.setStandCount(((Number) row[1]).longValue());
            dto.setTotalArea(((Number) row[2]).doubleValue());
            dto.setTotalVolume(((Number) row[3]).doubleValue());
            dto.setAvgVolumePerHa(((Number) row[4]).doubleValue());
            stats.add(dto);
        }
        return stats;
    }

    // 转换为DTO（用于前端展示）
    private StandDTO convertToDTO(ForestStand stand) {
        StandDTO dto = new StandDTO();
        dto.setStandId(stand.getStandId());
        dto.setXiaoBanCode(stand.getXiaoBanCode());
        dto.setStandName(stand.getStandName());
        dto.setAreaHa(stand.getAreaHa());
        dto.setDominantSpecies(stand.getDominantSpecies());
        dto.setStandAge(stand.getStandAge());
        dto.setVolumePerHa(stand.getVolumePerHa());
        dto.setTotalVolume(stand.getTotalVolume());
        dto.setCanopyDensity(stand.getCanopyDensity());
        dto.setCenterLon(stand.getCenterLon());
        dto.setCenterLat(stand.getCenterLat());
        dto.setOrigin(stand.getOrigin());
        return dto;
    }
}
