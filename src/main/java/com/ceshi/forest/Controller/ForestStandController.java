package com.ceshi.forest.controller;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.ForestStandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stands")
@CrossOrigin(origins = "*")
public class ForestStandController {

    private static final Logger logger = LoggerFactory.getLogger(ForestStandController.class);

    @Autowired
    private ForestStandService standService;

    @GetMapping
    public ResponseEntity<List<StandDTO>> getAllStands() {
        logger.info("收到请求: GET /api/stands");
        try {
            List<StandDTO> result = standService.getAllStands();
            logger.info("响应: GET /api/stands, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/stands, 错误: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandDTO> getStandById(@PathVariable Integer id) {
        logger.info("收到请求: GET /api/stands/{}", id);
        try {
            StandDTO result = standService.getStandById(id);
            logger.info("响应: GET /api/stands/{}, 成功", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/stands/{}, 错误: {}", id, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<StandDTO>> getNearbyStands(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam(defaultValue = "45000") Integer radiusMeters) {
        logger.info("收到请求: GET /api/stands/nearby, 坐标: ({}, {}), 半径: {}m", lon, lat, radiusMeters);
        try {
            List<StandDTO> result = standService.getNearbyStands(lon, lat, radiusMeters);
            logger.info("响应: GET /api/stands/nearby, 找到 {} 个林分", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/stands/nearby, 错误: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<StandDTO>> getHighValueStands(
            @RequestParam(defaultValue = "120") Double minVolumePerHa) {
        logger.info("收到请求: GET /api/stands/high-value, 最小蓄积: {}", minVolumePerHa);
        try {
            List<StandDTO> result = standService.getHighValueStands(minVolumePerHa);
            logger.info("响应: GET /api/stands/high-value, 找到 {} 个林分", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/stands/high-value, 错误: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/statistics/species")
    public ResponseEntity<List<StatisticsDTO>> getSpeciesStatistics() {
        logger.info("收到请求: GET /api/stands/statistics/species");
        try {
            List<StatisticsDTO> result = standService.getSpeciesStatistics();
            logger.info("响应: GET /api/stands/statistics/species, 返回 {} 种树种统计", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/stands/statistics/species, 错误: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.debug("健康检查请求");
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "forest-stand-service");
        return ResponseEntity.ok(status);
    }
}