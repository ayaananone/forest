package com.ceshi.forest.controller;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.service.SamplePlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plots")
@CrossOrigin(origins = "*")
public class SamplePlotController {

    private static final Logger logger = LoggerFactory.getLogger(SamplePlotController.class);

    @Autowired
    private SamplePlotService plotService;

    @GetMapping
    public ResponseEntity<List<PlotDTO>> getAllPlots() {
        logger.info("收到请求: GET /api/plots");
        try {
            List<PlotDTO> result = plotService.getAllPlots();
            logger.info("响应: GET /api/plots, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlotDTO> getPlotById(@PathVariable Integer id) {
        logger.info("收到请求: GET /api/plots/{}", id);
        try {
            PlotDTO result = plotService.getPlotById(id);
            logger.info("响应: GET /api/plots/{}, 成功", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots/{}, 错误: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<PlotDTO>> getPlotsByStandId(@PathVariable Integer standId) {
        logger.info("收到请求: GET /api/plots/stand/{}", standId);
        try {
            List<PlotDTO> result = plotService.getPlotsByStandId(standId);
            logger.info("响应: GET /api/plots/stand/{}, 返回 {} 条数据", standId, result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots/stand/{}, 错误: {}", standId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/high-volume")
    public ResponseEntity<List<PlotDTO>> getHighVolumePlots(
            @RequestParam(defaultValue = "150") Double minVolumePerHa) {
        logger.info("收到请求: GET /api/plots/high-volume, 最小蓄积: {}", minVolumePerHa);
        try {
            List<PlotDTO> result = plotService.getHighVolumePlots(minVolumePerHa);
            logger.info("响应: GET /api/plots/high-volume, 返回 {} 条数据", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots/high-volume, 错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<Map<String, Object>> getPlotStatistics(@PathVariable Integer standId) {
        logger.info("收到请求: GET /api/plots/stand/{}/statistics", standId);
        try {
            Map<String, Object> result = plotService.getPlotStatistics(standId);
            logger.info("响应: GET /api/plots/stand/{}/statistics, 成功", standId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots/stand/{}/statistics, 错误: {}", standId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{plotId}/verify")
    public ResponseEntity<Map<String, Object>> verifyPlotVolume(@PathVariable Integer plotId) {
        logger.info("收到请求: GET /api/plots/{}/verify", plotId);
        try {
            Map<String, Object> result = plotService.verifyPlotVolume(plotId);
            logger.info("响应: GET /api/plots/{}/verify, 状态: {}", plotId, result.get("status"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("请求处理失败: GET /api/plots/{}/verify, 错误: {}", plotId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        logger.debug("健康检查请求: GET /api/plots/health");
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "sample-plot-service");
        return ResponseEntity.ok(status);
    }
}