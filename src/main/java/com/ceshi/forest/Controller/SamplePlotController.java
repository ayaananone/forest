package com.ceshi.forest.controller;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.service.SamplePlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plots")
@RequiredArgsConstructor
public class SamplePlotController {

    private final SamplePlotService plotService;

    @GetMapping
    public ResponseEntity<List<PlotDTO>> getAllPlots() {
        return ResponseEntity.ok(plotService.getAllPlots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlotDTO> getPlotById(@PathVariable Integer id) {
        return ResponseEntity.ok(plotService.getPlotById(id));
    }

    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<PlotDTO>> getPlotsByStandId(@PathVariable Integer standId) {
        return ResponseEntity.ok(plotService.getPlotsByStandId(standId));
    }

    @GetMapping("/high-volume")
    public ResponseEntity<List<PlotDTO>> getHighVolumePlots(
            @RequestParam(defaultValue = "150") Double minVolumePerHa) {
        return ResponseEntity.ok(plotService.getHighVolumePlots(minVolumePerHa));
    }

    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<Map<String, Object>> getPlotStatistics(@PathVariable Integer standId) {
        return ResponseEntity.ok(plotService.getPlotStatistics(standId));
    }

    @GetMapping("/{plotId}/verify")
    public ResponseEntity<Map<String, Object>> verifyPlotVolume(@PathVariable Integer plotId) {
        return ResponseEntity.ok(plotService.verifyPlotVolume(plotId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "sample-plot-service");
        return ResponseEntity.ok(status);
    }
}