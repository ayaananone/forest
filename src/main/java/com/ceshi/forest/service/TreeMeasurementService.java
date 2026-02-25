package com.ceshi.forest.service;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.entity.TreeMeasurement;
import com.ceshi.forest.repository.TreeMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TreeMeasurementService {

    @Autowired
    private TreeMeasurementRepository treeRepository;

    // 获取所有单木
    public List<TreeDTO> getAllTrees() {
        return treeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据ID获取单木
    public TreeDTO getTreeById(Integer id) {
        TreeMeasurement tree = treeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("单木记录不存在"));
        return convertToDTO(tree);
    }

    // 根据样地ID获取单木
    public List<TreeDTO> getTreesByPlotId(Integer plotId) {
        return treeRepository.findByPlotPlotId(plotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据小班编号(stand_id)获取单木
    public List<TreeDTO> getTreesByStandId(Integer standId) {
        return treeRepository.findByStandStandId(standId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取大径级林木
    public List<TreeDTO> getLargeTrees(Double minDbh) {
        return treeRepository.findByDbhAvgGreaterThanEqual(minDbh).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 按树种查询
    public List<TreeDTO> getTreesBySpecies(String species) {
        return treeRepository.findBySpecies(species).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取全局树种统计
    public List<Map<String, Object>> getSpeciesStatistics() {
        List<Object[]> results = treeRepository.getStatisticsBySpecies();
        List<Map<String, Object>> stats = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("species", row[0]);
            map.put("treeCount", row[1]);
            map.put("totalVolume", row[2]);
            map.put("avgDbh", row[3]);
            map.put("avgHeight", row[4]);
            stats.add(map);
        }
        return stats;
    }

    // 获取小班(stand_id)内的树种统计
    public List<Map<String, Object>> getStandSpeciesStatistics(Integer standId) {
        List<TreeMeasurement> trees = treeRepository.findByStandStandId(standId);

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

    // 获取Top N大树
    public List<TreeDTO> getTopTrees(Integer limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "dbhAvg"));
        return treeRepository.findAll(pageRequest).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 转换为DTO - 通过关联对象获取ID
    private TreeDTO convertToDTO(TreeMeasurement tree) {
        TreeDTO dto = new TreeDTO();
        dto.setTreeId(tree.getTreeId());

        // 通过关联对象获取plotId（需要判空避免NPE）
        if (tree.getPlot() != null) {
            dto.setPlotId(tree.getPlot().getPlotId());
        }

        // 通过关联对象获取standId（小班编号）
        if (tree.getStand() != null) {
            dto.setStandId(tree.getStand().getStandId());
        }

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