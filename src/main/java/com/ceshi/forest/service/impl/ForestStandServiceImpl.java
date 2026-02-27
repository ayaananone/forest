package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import com.ceshi.forest.mapper.ForestStandMapper;
import com.ceshi.forest.service.ForestStandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 林分服务实现类
 */
@Service
@RequiredArgsConstructor
public class ForestStandServiceImpl implements ForestStandService {

    private final ForestStandMapper standMapper;

    @Override
    public List<StandDTO> getAllStands() {
        return standMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StandDTO getStandById(Integer id) {
        ForestStand stand = standMapper.findById(id);
        if (stand == null) {
            throw new RuntimeException("林分不存在");
        }
        return convertToDTO(stand);
    }

    @Override
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        return standMapper.findNearbyStands(lon, lat, radiusMeters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        return standMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsDTO> getSpeciesStatistics() {
        List<Map<String, Object>> results = standMapper.getStatisticsBySpecies();
        List<StatisticsDTO> stats = new ArrayList<>();

        for (Map<String, Object> row : results) {
            StatisticsDTO dto = new StatisticsDTO();
            dto.setSpecies((String) row.get("species"));
            dto.setStandCount(getLongValue(row.get("standCount")));
            dto.setTotalArea(getDoubleValue(row.get("totalArea")));
            dto.setTotalVolume(getDoubleValue(row.get("totalVolume")));
            dto.setAvgVolumePerHa(getDoubleValue(row.get("avgVolumePerHa")));
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

    private Long getLongValue(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }

    private Double getDoubleValue(Object obj) {
        return obj != null ? ((Number) obj).doubleValue() : 0.0;
    }
}