package com.ceshi.forest.Controller;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.service.SamplePlotService;
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

    @Autowired
    private SamplePlotService plotService;

    // 获取所有样地
    @GetMapping
    public ResponseEntity<List<PlotDTO>> getAllPlots() {
        return ResponseEntity.ok(plotService.getAllPlots());
    }

    // 根据ID获取样地
    @GetMapping("/{id}")
    public ResponseEntity<PlotDTO> getPlotById(@PathVariable Integer id) {
        return ResponseEntity.ok(plotService.getPlotById(id));
    }

    // 根据林分ID获取样地列表
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<PlotDTO>> getPlotsByStandId(@PathVariable Integer standId) {
        return ResponseEntity.ok(plotService.getPlotsByStandId(standId));
    }

    // 获取高蓄积样地
    @GetMapping("/high-volume")
    public ResponseEntity<List<PlotDTO>> getHighVolumePlots(
            @RequestParam(defaultValue = "150") Double minVolumePerHa) {
        return ResponseEntity.ok(plotService.getHighVolumePlots(minVolumePerHa));
    }

    // 获取样地统计信息
    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<Map<String, Object>> getPlotStatistics(@PathVariable Integer standId) {
        Map<String, Object> stats = plotService.getPlotStatistics(standId);
        return ResponseEntity.ok(stats);
    }

    // 验证样地蓄积（对比单木汇总和样地记录）
    @GetMapping("/{plotId}/verify")
    public ResponseEntity<Map<String, Object>> verifyPlotVolume(@PathVariable Integer plotId) {
        Map<String, Object> result = plotService.verifyPlotVolume(plotId);
        return ResponseEntity.ok(result);
    }

    // 健康检查
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "sample-plot-service");
        return ResponseEntity.ok(status);
    }
}