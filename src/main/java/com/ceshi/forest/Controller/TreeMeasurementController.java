package com.ceshi.forest.controller;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.service.TreeMeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(TreeMeasurementController.class);

    @Autowired
    private TreeMeasurementService treeService;

    @GetMapping
    public ResponseEntity<List<TreeDTO>> getAllTrees() {
        logger.info("收到请求: GET /api/trees");
        try {
            List<TreeDTO> result = treeService.getAllTrees();
            logger.info("响应: GET /api/trees, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreeDTO> getTreeById(@PathVariable Integer id) {
        logger.info("收到请求: GET /api/trees/{}", id);
        try {
            TreeDTO result = treeService.getTreeById(id);
            logger.info("响应: GET /api/trees/{}, 成功", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/{}, 错误: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/plot/{plotId}")
    public ResponseEntity<List<TreeDTO>> getTreesByPlotId(@PathVariable Integer plotId) {
        logger.info("收到请求: GET /api/trees/plot/{}", plotId);
        try {
            List<TreeDTO> result = treeService.getTreesByPlotId(plotId);
            logger.info("响应: GET /api/trees/plot/{}, 返回 {} 条数据", plotId, result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/plot/{}, 错误: {}", plotId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<TreeDTO>> getTreesByStandId(@PathVariable Integer standId) {
        logger.info("收到请求: GET /api/trees/stand/{}", standId);
        try {
            List<TreeDTO> result = treeService.getTreesByStandId(standId);
            logger.info("响应: GET /api/trees/stand/{}, 返回 {} 条数据", standId, result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/stand/{}, 错误: {}", standId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/large-trees")
    public ResponseEntity<List<TreeDTO>> getLargeTrees(
            @RequestParam(defaultValue = "30") Double minDbh) {
        logger.info("收到请求: GET /api/trees/large-trees, 最小胸径: {}", minDbh);
        try {
            List<TreeDTO> result = treeService.getLargeTrees(minDbh);
            logger.info("响应: GET /api/trees/large-trees, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/large-trees, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/species/{species}")
    public ResponseEntity<List<TreeDTO>> getTreesBySpecies(@PathVariable String species) {
        logger.info("收到请求: GET /api/trees/species/{}", species);
        try {
            List<TreeDTO> result = treeService.getTreesBySpecies(species);
            logger.info("响应: GET /api/trees/species/{}, 返回 {} 条数据", species, result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/species/{}, 错误: {}", species, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/statistics/species")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesStatistics() {
        logger.info("收到请求: GET /api/trees/statistics/species");
        try {
            List<Map<String, Object>> result = treeService.getSpeciesStatistics();
            logger.info("响应: GET /api/trees/statistics/species, 返回 {} 种树种", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/statistics/species, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<List<Map<String, Object>>> getStandSpeciesStatistics(@PathVariable Integer standId) {
        logger.info("收到请求: GET /api/trees/stand/{}/statistics", standId);
        try {
            List<Map<String, Object>> result = treeService.getStandSpeciesStatistics(standId);
            logger.info("响应: GET /api/trees/stand/{}/statistics, 返回 {} 种树种", standId, result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/stand/{}/statistics, 错误: {}", standId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/top-trees")
    public ResponseEntity<List<TreeDTO>> getTopTrees(
            @RequestParam(defaultValue = "10") Integer limit) {
        logger.info("收到请求: GET /api/trees/top-trees, 限制: {}", limit);
        try {
            List<TreeDTO> result = treeService.getTopTrees(limit);
            logger.info("响应: GET /api/trees/top-trees, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/trees/top-trees, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.debug("健康检查请求: GET /api/trees/health");
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "tree-measurement-service");
        return ResponseEntity.ok(status);
    }
}