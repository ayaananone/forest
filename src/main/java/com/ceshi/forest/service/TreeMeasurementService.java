package com.ceshi.forest.service;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.entity.TreeMeasurement;
import com.ceshi.forest.mapper.TreeMeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreeMeasurementService {

    @Autowired
    private TreeMeasurementMapper treeMapper;

    public List<TreeDTO> getAllTrees() {
        return treeMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TreeDTO getTreeById(Integer id) {
        TreeMeasurement tree = treeMapper.findById(id);
        if (tree == null) {
            throw new RuntimeException("单木记录不存在");
        }
        return convertToDTO(tree);
    }

    public List<TreeDTO> getTreesByPlotId(Integer plotId) {
        return treeMapper.findByPlotId(plotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TreeDTO> getTreesByStandId(Integer standId) {
        return treeMapper.findByStandId(standId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TreeDTO> getLargeTrees(Double minDbh) {
        return treeMapper.findByDbhAvgGreaterThanEqual(minDbh).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TreeDTO> getTreesBySpecies(String species) {
        return treeMapper.findBySpecies(species).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSpeciesStatistics() {
        List<Map<String, Object>> results = treeMapper.getStatisticsBySpecies();
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Map<String, Object> row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("species", row.get("species"));
            map.put("treeCount", row.get("treeCount") != null ? row.get("treeCount") : 0);
            map.put("totalVolume", row.get("totalVolume") != null ? row.get("totalVolume") : 0.0);
            map.put("avgDbh", row.get("avgDbh") != null ? row.get("avgDbh") : 0.0);
            map.put("avgHeight", row.get("avgHeight") != null ? row.get("avgHeight") : 0.0);
            stats.add(map);
        }
        return stats;
    }

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
                .sorted((a, b) -> ((Integer)b.get("treeCount")).compareTo((Integer)a.get("treeCount")))
                .collect(Collectors.toList());
    }

    public List<TreeDTO> getTopTrees(Integer limit) {
        return treeMapper.findAll().stream()
                .sorted((a, b) -> b.getDbhAvg().compareTo(a.getDbhAvg()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TreeDTO convertToDTO(TreeMeasurement tree) {
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
    }
}