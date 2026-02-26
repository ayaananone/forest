package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import com.ceshi.forest.mapper.ForestStandMapper;
import com.ceshi.forest.service.ForestStandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 林分服务实现类
 */
@Service
public class ForestStandServiceImpl implements ForestStandService {

    private static final Logger logger = LoggerFactory.getLogger(ForestStandServiceImpl.class);

    @Autowired
    private ForestStandMapper standMapper;

    @Override
    public List<StandDTO> getAllStands() {
        try {
            logger.info("获取所有林分数据");
            List<StandDTO> result = standMapper.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("成功获取 {} 条林分数据", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取所有林分数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取林分数据失败", e);
        }
    }

    @Override
    public StandDTO getStandById(Integer id) {
        try {
            logger.info("根据ID获取林分: {}", id);
            ForestStand stand = standMapper.findById(id);
            if (stand == null) {
                logger.warn("林分不存在: {}", id);
                throw new RuntimeException("林分不存在");
            }
            logger.info("成功获取林分: {}", stand.getStandName());
            return convertToDTO(stand);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取林分详情失败, ID={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("获取林分详情失败", e);
        }
    }

    @Override
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        try {
            logger.info("空间查询附近林分, 坐标: ({}, {}), 半径: {}米", lon, lat, radiusMeters);
            List<StandDTO> result = standMapper.findNearbyStands(lon, lat, radiusMeters).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("附近林分查询成功, 找到 {} 个林分", result.size());
            return result;
        } catch (Exception e) {
            logger.error("附近林分查询失败: {}", e.getMessage(), e);
            throw new RuntimeException("附近林分查询失败", e);
        }
    }

    @Override
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        try {
            logger.info("获取高价值林分, 最小蓄积: {} m³/ha", minVolumePerHa);
            List<StandDTO> result = standMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("高价值林分查询成功, 找到 {} 个林分", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取高价值林分失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取高价值林分失败", e);
        }
    }

    @Override
    public List<StatisticsDTO> getSpeciesStatistics() {
        try {
            logger.info("获取树种统计数据");
            List<Map<String, Object>> results = standMapper.getStatisticsBySpecies();
            List<StatisticsDTO> stats = new ArrayList<>();

            for (Map<String, Object> row : results) {
                try {
                    StatisticsDTO dto = new StatisticsDTO();
                    dto.setSpecies((String) row.get("species"));

                    Object standCount = row.get("standCount");
                    dto.setStandCount(standCount != null ? ((Number) standCount).longValue() : 0L);

                    Object totalArea = row.get("totalArea");
                    dto.setTotalArea(totalArea != null ? ((Number) totalArea).doubleValue() : 0.0);

                    Object totalVolume = row.get("totalVolume");
                    dto.setTotalVolume(totalVolume != null ? ((Number) totalVolume).doubleValue() : 0.0);

                    Object avgVolumePerHa = row.get("avgVolumePerHa");
                    dto.setAvgVolumePerHa(avgVolumePerHa != null ? ((Number) avgVolumePerHa).doubleValue() : 0.0);

                    stats.add(dto);
                } catch (Exception e) {
                    logger.warn("处理树种统计行数据失败: {}, 错误: {}", row, e.getMessage());
                }
            }

            logger.info("树种统计查询成功, 共 {} 种树种", stats.size());
            return stats;
        } catch (Exception e) {
            logger.error("获取树种统计失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取树种统计失败", e);
        }
    }

    private StandDTO convertToDTO(ForestStand stand) {
        try {
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
        } catch (Exception e) {
            logger.error("转换林分DTO失败, standId={}: {}", stand.getStandId(), e.getMessage());
            throw new RuntimeException("数据转换失败", e);
        }
    }
}