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

    /**
     * 创建新林分
     */
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
            // 主键冲突时自动修复序列并重试
            log.warn("检测到主键冲突，尝试自动修复序列...");
            fixSequenceInternal();

            // 重试创建
            StandDTO created = standService.createStand(standDTO, operator);
            standCacheService.refreshStand(created.getStandId());
            return ResponseEntity.ok(ResultDTO.ok(created, "林分创建成功（已自动修复序列）"));
        }
    }

    /**
     * 更新林分信息
     */
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

    /**
     * 删除林分
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResultDTO<Void>> deleteStand(
            @PathVariable Integer id,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        standService.deleteStand(id, operator);
        standCacheService.clearStandCache(id);

        return ResponseEntity.ok(ResultDTO.ok(null, "林分删除成功"));
    }

    /**
     * 批量更新林分
     */
    @PutMapping("/batch")
    public ResponseEntity<ResultDTO<List<StandDTO>>> batchUpdateStands(
            @RequestBody @Valid List<StandDTO> standDTOList,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        List<StandDTO> updated = standService.batchUpdateStands(standDTOList, operator);
        updated.forEach(stand -> standCacheService.refreshStand(stand.getStandId()));

        return ResponseEntity.ok(ResultDTO.ok(updated, "批量更新成功"));
    }

    /**
     * 批量删除林分
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResultDTO<Void>> batchDeleteStands(
            @RequestBody List<Integer> ids,
            Authentication authentication) {

        String operator = authentication != null ? authentication.getName() : "system";
        standService.batchDeleteStands(ids, operator);
        ids.forEach(standCacheService::clearStandCache);

        return ResponseEntity.ok(ResultDTO.ok(null, "批量删除成功"));
    }

    /**
     * 检查林分编号是否存在
     */
    @GetMapping("/check-code")
    public ResponseEntity<ResultDTO<Boolean>> checkStandCodeExists(
            @RequestParam String xiaoBanCode,
            @RequestParam(required = false) Integer excludeId) {

        boolean exists = standService.checkStandCodeExists(xiaoBanCode, excludeId);
        return ResponseEntity.ok(ResultDTO.ok(exists, exists ? "编号已存在" : "编号可用"));
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

    // ==================== 序列修复接口（解决主键冲突问题） ====================

    /**
     * 修复主键序列问题
     * 当出现"重复键违反唯一约束"错误时，调用此接口修复
     */
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

    /**
     * 内部修复序列方法
     */
    private void fixSequenceInternal() {
        // 获取当前表中最大ID
        Integer maxId = forestStandMapper.selectMaxStandId();
        if (maxId == null) {
            maxId = 0;
        }

        // 重置序列到最大ID + 1
        Integer nextVal = maxId + 1;
        forestStandMapper.resetSequence(nextVal);

        log.info("序列已修复：最大ID={}, 序列下一个值={}", maxId, nextVal);
    }

    /**
     * 查询序列状态
     */
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
