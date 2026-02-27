package com.ceshi.forest.controller;

import com.ceshi.forest.aspect.NoLog;
import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.StandCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stands")
@RequiredArgsConstructor
public class ForestStandController {

    private final StandCacheService standCacheService;

    @GetMapping
    public ResponseEntity<List<StandDTO>> getAllStands() {
        return ResponseEntity.ok(standCacheService.getAllStands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandDTO> getStandById(@PathVariable Integer id) {
        return ResponseEntity.ok(standCacheService.getStandById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<StandDTO>> getNearbyStands(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam(defaultValue = "45000") Integer radiusMeters) {
        return ResponseEntity.ok(standCacheService.getNearbyStands(lon, lat, radiusMeters));
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<StandDTO>> getHighValueStands(
            @RequestParam(defaultValue = "120") Double minVolumePerHa) {
        return ResponseEntity.ok(standCacheService.getHighValueStands(minVolumePerHa));
    }

    @GetMapping("/statistics/species")
    public ResponseEntity<List<StatisticsDTO>> getSpeciesStatistics() {
        return ResponseEntity.ok(standCacheService.getSpeciesStatistics());
    }

    @DeleteMapping("/cache/{id}")
    public ResponseEntity<Map<String, String>> clearStandCache(@PathVariable Integer id) {
        standCacheService.clearStandCache(id);
        Map<String, String> result = new HashMap<>();
        result.put("message", "林分缓存已清除");
        result.put("standId", String.valueOf(id));
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/cache/all")
    public ResponseEntity<Map<String, String>> clearAllStandCache() {
        standCacheService.clearAllStandCache();
        Map<String, String> result = new HashMap<>();
        result.put("message", "所有林分缓存已清除");
        return ResponseEntity.ok(result);
    }

    @NoLog
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "forest-stand-service");
        status.put("cache", "enabled");
        return ResponseEntity.ok(status);
    }
}