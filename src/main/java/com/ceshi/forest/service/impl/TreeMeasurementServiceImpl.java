package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.entity.TreeMeasurement;
import com.ceshi.forest.mapper.TreeMeasurementMapper;
import com.ceshi.forest.service.TreeMeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 单木测量服务实现类
 */
@Service
public class TreeMeasurementServiceImpl implements TreeMeasurementService {

    private static final Logger logger = LoggerFactory.getLogger(TreeMeasurementServiceImpl.class);

    @Autowired
    private TreeMeasurementMapper treeMapper;

    @Override
    public List<TreeDTO> getAllTrees() {
        try {
            logger.info("获取所有单木数据");
            List<TreeDTO> result = treeMapper.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("成功获取 {} 条单木数据", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取所有单木数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取单木数据失败", e);
        }
    }

    @Override
    public TreeDTO getTreeById(Integer id) {
        try {
            logger.info("根据ID获取单木: {}", id);
            TreeMeasurement tree = treeMapper.findById(id);
            if (tree == null) {
                logger.warn("单木记录不存在: {}", id);
                throw new RuntimeException("单木记录不存在");
            }
            logger.info("成功获取单木: {}", tree.getTreeId());
            return convertToDTO(tree);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取单木详情失败, ID={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("获取单木详情失败", e);
        }
    }

    @Override
    public List<TreeDTO> getTreesByPlotId(Integer plotId) {
        try {
            logger.info("获取样地下的单木列表, plotId: {}", plotId);
            List<TreeDTO> result = treeMapper.findByPlotId(plotId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("样地 {} 下找到 {} 株单木", plotId, result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取样地单木列表失败, plotId={}: {}", plotId, e.getMessage(), e);
            throw new RuntimeException("获取样地单木列表失败", e);
        }
    }

    @Override
    public List<TreeDTO> getTreesByStandId(Integer standId) {
        try {
            logger.info("获取林分下的单木列表, standId: {}", standId);
            List<TreeDTO> result = treeMapper.findByStandId(standId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("林分 {} 下找到 {} 株单木", standId, result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取林分单木列表失败, standId={}: {}", standId, e.getMessage(), e);
            throw new RuntimeException("获取林分单木列表失败", e);
        }
    }

    @Override
    public List<TreeDTO> getLargeTrees(Double minDbh) {
        try {
            logger.info("获取大径级林木, 最小胸径: {} cm", minDbh);
            List<TreeDTO> result = treeMapper.findByDbhAvgGreaterThanEqual(minDbh).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("大径级林木查询成功, 找到 {} 株", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取大径级林木失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取大径级林木失败", e);
        }
    }

    @Override
    public List<TreeDTO> getTreesBySpecies(String species) {
        try {
            logger.info("按树种查询单木, 树种: {}", species);
            List<TreeDTO> result = treeMapper.findBySpecies(species).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("树种 {} 查询成功, 找到 {} 株", species, result.size());
            return result;
        } catch (Exception e) {
            logger.error("按树种查询单木失败, species={}: {}", species, e.getMessage(), e);
            throw new RuntimeException("按树种查询单木失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getSpeciesStatistics() {
        try {
            logger.info("获取全局树种统计");
            List<Map<String, Object>> results = treeMapper.getStatisticsBySpecies();
            List<Map<String, Object>> stats = new ArrayList<>();

            for (Map<String, Object> row : results) {
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("species", row.get("species"));
                    map.put("treeCount", row.get("treeCount") != null ? row.get("treeCount") : 0);
                    map.put("totalVolume", row.get("totalVolume") != null ? row.get("totalVolume") : 0.0);
                    map.put("avgDbh", row.get("avgDbh") != null ? row.get("avgDbh") : 0.0);
                    map.put("avgHeight", row.get("avgHeight") != null ? row.get("avgHeight") : 0.0);
                    stats.add(map);
                } catch (Exception e) {
                    logger.warn("处理单木统计行数据失败: {}, 错误: {}", row, e.getMessage());
                }
            }

            logger.info("全局树种统计查询成功, 共 {} 种树种", stats.size());
            return stats;
        } catch (Exception e) {
            logger.error("获取树种统计失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取树种统计失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getStandSpeciesStatistics(Integer standId) {
        try {
            logger.info("获取林分内树种统计, standId: {}", standId);
            List<TreeMeasurement> trees = treeMapper.findByStandId(standId);

            Map<String, List<TreeMeasurement>> grouped = trees.stream()
                    .collect(Collectors.groupingBy(TreeMeasurement::getSpecies));

            List<Map<String, Object>> stats = new ArrayList<>();
            grouped.forEach((species, treeList) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("species", species);
                map.put("treeCount", treeList.size());
                map.put("totalVolume", treeList.stream().mapToDouble(TreeMeasurement::getVolume).sum());
                map.put("avgDbh", treeList.stream().mapToDouble(TreeMeasurement::getDbhAvg).average().orElse(0));
                map.put("avgHeight", treeList.stream().mapToDouble(TreeMeasurement::getTreeHeight).average().orElse(0));
                stats.add(map);
            });

            logger.info("林分 {} 树种统计完成, 共 {} 种树种", standId, stats.size());
            return stats.stream()
                    .sorted((a, b) -> ((Integer)b.get("treeCount")).compareTo((Integer)a.get("treeCount")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取林分树种统计失败, standId={}: {}", standId, e.getMessage(), e);
            throw new RuntimeException("获取林分树种统计失败", e);
        }
    }

    @Override
    public List<TreeDTO> getTopTrees(Integer limit) {
        try {
            logger.info("获取Top {} 大树", limit);
            List<TreeDTO> result = treeMapper.findAll().stream()
                    .sorted((a, b) -> b.getDbhAvg().compareTo(a.getDbhAvg()))
                    .limit(limit)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("Top {} 大树查询成功", limit);
            return result;
        } catch (Exception e) {
            logger.error("获取Top大树失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取Top大树失败", e);
        }
    }

    private TreeDTO convertToDTO(TreeMeasurement tree) {
        try {
            TreeDTO dto = new TreeDTO();
            dto.setTreeId(tree.getTreeId());
            dto.setPlotId(tree.getPlotId());
            dto.setStandId(tree.getStandId());
            dto.setTreeNo(tree.getTreeNo());
            dto.setSpecies(tree.getSpecies());
            dto.setSpeciesCode(tree.getSpeciesCode());
            dto.setDbhDirection1(tree.getDbhDirection1());
            dto.setDbhDirection2(tree.getDbhDirection2());
            dto.setDbhAvg(tree.getDbhAvg());
            dto.setTreeHeight(tree.getTreeHeight());
            dto.setDiameterHalfHeight(tree.getDiameterHalfHeight());
            dto.setQ2(tree.getQ2());
            dto.setF1(tree.getF1());
            dto.setBasalArea(tree.getBasalArea());
            dto.setVolume(tree.getVolume());
            dto.setCrownWidth(tree.getCrownWidth());
            dto.setTreeQuality(tree.getTreeQuality());
            dto.setHealthStatus(tree.getHealthStatus());
            dto.setSurveyDate(tree.getSurveyDate() != null ? tree.getSurveyDate().toString() : null);
            return dto;
        } catch (Exception e) {
            logger.error("转换单木DTO失败, treeId={}: {}", tree.getTreeId(), e.getMessage());
            throw new RuntimeException("数据转换失败", e);
        }
    }
    @Override
    public List<TreeDTO> getTreesByStandIdString(String standId) {
        logger.info("根据字符串林分ID获取单木列表, standId: {}", standId);

        try {
            logger.warn("字符串林分ID查询暂未完全实现，返回空列表。standId: {}", standId);
            return List.of();

        } catch (Exception e) {
            logger.error("根据字符串林分ID获取单木列表失败, standId={}: {}", standId, e.getMessage(), e);
            return List.of();
        }
    }
}