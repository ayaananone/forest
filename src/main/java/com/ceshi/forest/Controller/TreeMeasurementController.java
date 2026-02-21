package com.ceshi.forest.Controller;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.service.TreeMeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trees")
@CrossOrigin(origins = "*")
public class TreeMeasurementController {

    @Autowired
    private TreeMeasurementService treeService;

    // 获取所有单木
    @GetMapping
    public ResponseEntity<List<TreeDTO>> getAllTrees() {
        return ResponseEntity.ok(treeService.getAllTrees());
    }

    // 根据ID获取单木
    @GetMapping("/{id}")
    public ResponseEntity<TreeDTO> getTreeById(@PathVariable Integer id) {
        return ResponseEntity.ok(treeService.getTreeById(id));
    }

    // 根据样地ID获取单木列表
    @GetMapping("/plot/{plotId}")
    public ResponseEntity<List<TreeDTO>> getTreesByPlotId(@PathVariable Integer plotId) {
        return ResponseEntity.ok(treeService.getTreesByPlotId(plotId));
    }

    // 根据林分ID获取单木列表
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<TreeDTO>> getTreesByStandId(@PathVariable Integer standId) {
        return ResponseEntity.ok(treeService.getTreesByStandId(standId));
    }

    // 获取大径级林木（胸径>=指定值）
    @GetMapping("/large-trees")
    public ResponseEntity<List<TreeDTO>> getLargeTrees(
            @RequestParam(defaultValue = "30") Double minDbh) {
        return ResponseEntity.ok(treeService.getLargeTrees(minDbh));
    }

    // 按树种查询单木
    @GetMapping("/species/{species}")
    public ResponseEntity<List<TreeDTO>> getTreesBySpecies(@PathVariable String species) {
        return ResponseEntity.ok(treeService.getTreesBySpecies(species));
    }

    // 获取树种统计（全局）
    @GetMapping("/statistics/species")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesStatistics() {
        return ResponseEntity.ok(treeService.getSpeciesStatistics());
    }

    // 获取林分内的树种统计
    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<List<Map<String, Object>>> getStandSpeciesStatistics(@PathVariable Integer standId) {
        return ResponseEntity.ok(treeService.getStandSpeciesStatistics(standId));
    }

    // 获取Top N大树
    @GetMapping("/top-trees")
    public ResponseEntity<List<TreeDTO>> getTopTrees(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(treeService.getTopTrees(limit));
    }

    // 健康检查
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "tree-measurement-service");
        return ResponseEntity.ok(status);
    }
}