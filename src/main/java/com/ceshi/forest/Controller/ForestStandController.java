package com.ceshi.forest.controller;

import com.ceshi.forest.aspect.NoLog;
import com.ceshi.forest.dto.ResultDTO;
import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.StandCacheService;
import com.ceshi.forest.service.ForestStandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/stands")
@RequiredArgsConstructor
@Validated
public class ForestStandController {

    private final StandCacheService standCacheService;
    private final ForestStandService standService;

    // ==================== 原有查询接口 ====================

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

    // ==================== 新增CRUD接口 ====================

    @PostMapping
    public ResponseEntity<ResultDTO<StandDTO>> createStand(
            @RequestBody @Valid StandDTO standDTO,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        StandDTO created = standService.createStand(standDTO, operator);
        standCacheService.refreshStand(created.getStandId());

        return ResponseEntity.ok(ResultDTO.ok(created, "林分创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultDTO<StandDTO>> updateStand(
            @PathVariable Integer id,
            @RequestBody @Valid StandDTO standDTO,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        standDTO.setStandId(id);
        StandDTO updated = standService.updateStand(standDTO, operator);
        standCacheService.refreshStand(id);

        return ResponseEntity.ok(ResultDTO.ok(updated, "林分更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResultDTO<Void>> deleteStand(
            @PathVariable Integer id,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        standService.deleteStand(id, operator);
        standCacheService.clearStandCache(id);

        return ResponseEntity.ok(ResultDTO.ok(null, "林分删除成功"));
    }

    @PutMapping("/batch")
    public ResponseEntity<ResultDTO<List<StandDTO>>> batchUpdateStands(
            @RequestBody @Valid List<StandDTO> standDTOList,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        List<StandDTO> updated = standService.batchUpdateStands(standDTOList, operator);
        updated.forEach(stand -> standCacheService.refreshStand(stand.getStandId()));

        return ResponseEntity.ok(ResultDTO.ok(updated, "批量更新成功"));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ResultDTO<Void>> batchDeleteStands(
            @RequestBody List<Integer> ids,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        standService.batchDeleteStands(ids, operator);
        ids.forEach(standCacheService::clearStandCache);

        return ResponseEntity.ok(ResultDTO.ok(null, "批量删除成功"));
    }

    @GetMapping("/check-code")
    public ResponseEntity<ResultDTO<Boolean>> checkStandCodeExists(
            @RequestParam String xiaoBanCode,
            @RequestParam(required = false) Integer excludeId) {

        boolean exists = standService.checkStandCodeExists(xiaoBanCode, excludeId);
        return ResponseEntity.ok(ResultDTO.ok(exists, exists ? "编号已存在" : "编号可用"));
    }

    @GetMapping("/code/{xiaoBanCode}")
    public ResponseEntity<ResultDTO<StandDTO>> getStandByCode(@PathVariable String xiaoBanCode) {
        StandDTO stand = standService.getStandByCode(xiaoBanCode);
        return ResponseEntity.ok(ResultDTO.ok(stand));
    }

    // ==================== 缓存管理接口 ====================

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