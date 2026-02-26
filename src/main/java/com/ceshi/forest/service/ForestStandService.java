package com.ceshi.forest.service;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import com.ceshi.forest.mapper.ForestStandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForestStandService {

    @Autowired
    private ForestStandMapper standMapper;

    public List<StandDTO> getAllStands() {
        return standMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StandDTO getStandById(Integer id) {
        ForestStand stand = standMapper.findById(id);
        if (stand == null) {
            throw new RuntimeException("林分不存在");
        }
        return convertToDTO(stand);
    }

    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        return standMapper.findNearbyStands(lon, lat, radiusMeters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        return standMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StatisticsDTO> getSpeciesStatistics() {
        List<Map<String, Object>> results = standMapper.getStatisticsBySpecies();
        List<StatisticsDTO> stats = new ArrayList<>();

        for (Map<String, Object> row : results) {
            StatisticsDTO dto = new StatisticsDTO();
            dto.setSpecies((String) row.get("species"));

            // 添加空值检查
            Object standCount = row.get("standCount");
            dto.setStandCount(standCount != null ? ((Number) standCount).longValue() : 0L);

            Object totalArea = row.get("totalArea");
            dto.setTotalArea(totalArea != null ? ((Number) totalArea).doubleValue() : 0.0);

            Object totalVolume = row.get("totalVolume");
            dto.setTotalVolume(totalVolume != null ? ((Number) totalVolume).doubleValue() : 0.0);

            Object avgVolumePerHa = row.get("avgVolumePerHa");
            dto.setAvgVolumePerHa(avgVolumePerHa != null ? ((Number) avgVolumePerHa).doubleValue() : 0.0);

            stats.add(dto);
        }
        return stats;
    }

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