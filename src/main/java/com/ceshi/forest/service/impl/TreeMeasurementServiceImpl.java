package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.entity.TreeMeasurement;
import com.ceshi.forest.mapper.TreeMeasurementMapper;
import com.ceshi.forest.service.TreeMeasurementService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TreeMeasurementServiceImpl implements TreeMeasurementService {

    private final TreeMeasurementMapper treeMapper;

    @Override
    public List<TreeDTO> getAllTrees() {
        return treeMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TreeDTO getTreeById(Integer id) {
        TreeMeasurement tree = treeMapper.findById(id);
        if (tree == null) {
            throw new RuntimeException("单木记录不存在");
        }
        return convertToDTO(tree);
    }

    @Override
    public List<TreeDTO> getTreesByPlotId(Integer plotId) {
        return treeMapper.findByPlotId(plotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeDTO> getTreesByStandId(Integer standId) {
        return treeMapper.findByStandId(standId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeDTO> getLargeTrees(Double minDbh) {
        return treeMapper.findByDbhAvgGreaterThanEqual(minDbh).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeDTO> getTreesBySpecies(String species) {
        return treeMapper.findBySpecies(species).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getSpeciesStatistics() {
        List<Map<String, Object>> results = treeMapper.getStatisticsBySpecies();
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Map<String, Object> row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("species", row.get("species"));
            map.put("treeCount", row.getOrDefault("treeCount", 0));
            map.put("totalVolume", row.getOrDefault("totalVolume", 0.0));
            map.put("avgDbh", row.getOrDefault("avgDbh", 0.0));
            map.put("avgHeight", row.getOrDefault("avgHeight", 0.0));
            stats.add(map);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getStandSpeciesStatistics(Integer standId) {
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

        return stats.stream()
                .sorted((a, b) -> ((Integer) b.get("treeCount")).compareTo((Integer) a.get("treeCount")))
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeDTO> getTopTrees(Integer limit) {
        return treeMapper.findAll().stream()
                .sorted((a, b) -> b.getDbhAvg().compareTo(a.getDbhAvg()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TreeDTO> getTreesByStandIdString(String standId) {
        return List.of();
    }

    private TreeDTO convertToDTO(TreeMeasurement tree) {
        TreeDTO dto = new TreeDTO();
        dto.setTreeId(tree.getTreeId());
        dto.setPlotId(tree.getPlotId());
        dto.setTreeNo(tree.getTreeNo());
        dto.setSpecies(tree.getSpecies());
        dto.setDbhAvg(tree.getDbhAvg());
        dto.setTreeHeight(tree.getTreeHeight());
        dto.setDiameterHalfHeight(tree.getDiameterHalfHeight());
        dto.setQ2(tree.getQ2());
        dto.setF1(tree.getF1());
        dto.setBasalArea(tree.getBasalArea());
        dto.setVolume(tree.getVolume());
        dto.setCrownWidth(tree.getCrownWidth());
        dto.setDbhDirection1(tree.getDbhDirection1());
        dto.setDbhDirection2(tree.getDbhDirection2());
        dto.setHealthStatus(tree.getHealthStatus());
        dto.setSpeciesCode(tree.getSpeciesCode());
        dto.setSurveyDate(tree.getSurveyDate() != null ? tree.getSurveyDate().toString() : null);
        dto.setTreeQuality(tree.getTreeQuality());
        dto.setStandId(tree.getStandId());
        return dto;
    }
}