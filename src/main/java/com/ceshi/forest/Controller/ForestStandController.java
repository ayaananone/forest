package com.ceshi.forest.controller;

import com.ceshi.forest.aspect.NoLog;
import com.ceshi.forest.dto.ResultDTO;
import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.mapper.ForestStandMapper;
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
    private final ForestStandMapper forestStandMapper;

    // ==================== 查询接口 ====================

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
    public List<StatisticsDTO> getSpeciesStatistics() {
        // 从缓存获取或从数据库查询
        return forestStandMapper.getStatisticsBySpecies();
    }

    // ==================== CRUD接口 ====================

    @PostMapping
    public ResponseEntity<ResultDTO<StandDTO>> createStand(
            @RequestBody @Valid StandDTO standDTO,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";

        try {
            StandDTO created = standService.createStand(standDTO, operator);
            standCacheService.refreshStand(created.getStandId());
            return ResponseEntity.ok(ResultDTO.ok(created, "林分创建成功"));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("检测到主键冲突，尝试自动修复序列...");
            fixSequenceInternal();

            StandDTO created = standService.createStand(standDTO, operator);
            standCacheService.refreshStand(created.getStandId());
            return ResponseEntity.ok(ResultDTO.ok(created, "林分创建成功（已自动修复序列）"));
        }
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

    @GetMapping("/check-id/{id}")
    public ResponseEntity<ResultDTO<Boolean>> checkStandIdExists(@PathVariable Integer id) {
        boolean exists = standService.getStandById(id) != null;
        return ResponseEntity.ok(ResultDTO.ok(exists, exists ? "林分存在" : "林分不存在"));
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

    // ==================== 序列修复接口 ====================

    @PostMapping("/fix-sequence")
    public ResponseEntity<Map<String, Object>> fixSequence() {
        Map<String, Object> result = new HashMap<>();
        try {
            fixSequenceInternal();
            result.put("success", true);
            result.put("message", "序列修复成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("修复序列失败", e);
            result.put("success", false);
            result.put("message", "修复序列失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    private void fixSequenceInternal() {
        Integer maxId = forestStandMapper.selectMaxStandId();
        if (maxId == null) {
            maxId = 0;
        }

        Integer nextVal = maxId + 1;
        forestStandMapper.resetSequence(nextVal);

        log.info("序列已修复：最大ID={}, 序列下一个值={}", maxId, nextVal);
    }

    @GetMapping("/sequence-status")
    public ResponseEntity<Map<String, Object>> getSequenceStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer maxId = forestStandMapper.selectMaxStandId();
            Integer currentSeq = forestStandMapper.getCurrentSequenceValue();

            result.put("maxId", maxId);
            result.put("currentSequenceValue", currentSeq);
            result.put("needFix", currentSeq != null && maxId != null && currentSeq <= maxId);
            result.put("message", currentSeq != null && maxId != null && currentSeq <= maxId
                    ? "序列需要修复，当前序列值小于或等于最大ID"
                    : "序列状态正常");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("查询序列状态失败", e);
            result.put("success", false);
            result.put("message", "查询序列状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
