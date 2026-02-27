package com.ceshi.forest.controller;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.service.TreeMeasurementService;
import com.ceshi.forest.util.ExportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/trees")
@RequiredArgsConstructor
public class TreeMeasurementController {

    private final TreeMeasurementService treeService;
    private final ExportUtil exportUtil;

    @GetMapping
    public ResponseEntity<List<TreeDTO>> getAllTrees() {
        return ResponseEntity.ok(treeService.getAllTrees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreeDTO> getTreeById(@PathVariable Integer id) {
        return ResponseEntity.ok(treeService.getTreeById(id));
    }

    @GetMapping("/plot/{plotId}")
    public ResponseEntity<List<TreeDTO>> getTreesByPlotId(@PathVariable Integer plotId) {
        return ResponseEntity.ok(treeService.getTreesByPlotId(plotId));
    }

    /**
     * 根据林分ID获取单木列表 - 支持数字和字符串类型的林分ID
     */
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<TreeDTO>> getTreesByStandId(@PathVariable String standId) {
        List<TreeDTO> result;
        try {
            Integer id = Integer.parseInt(standId);
            result = treeService.getTreesByStandId(id);
        } catch (NumberFormatException e) {
            log.warn("林分ID '{}' 不是数字格式，尝试字符串查询", standId);
            result = treeService.getTreesByStandIdString(standId);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/large-trees")
    public ResponseEntity<List<TreeDTO>> getLargeTrees(
            @RequestParam(defaultValue = "30") Double minDbh) {
        return ResponseEntity.ok(treeService.getLargeTrees(minDbh));
    }

    @GetMapping("/species/{species}")
    public ResponseEntity<List<TreeDTO>> getTreesBySpecies(@PathVariable String species) {
        return ResponseEntity.ok(treeService.getTreesBySpecies(species));
    }

    @GetMapping("/statistics/species")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesStatistics() {
        return ResponseEntity.ok(treeService.getSpeciesStatistics());
    }

    @GetMapping("/stand/{standId}/statistics")
    public ResponseEntity<List<Map<String, Object>>> getStandSpeciesStatistics(@PathVariable String standId) {
        List<TreeDTO> trees;
        try {
            Integer id = Integer.parseInt(standId);
            trees = treeService.getTreesByStandId(id);
        } catch (NumberFormatException e) {
            trees = treeService.getTreesByStandIdString(standId);
        }

        Map<String, List<TreeDTO>> grouped = trees.stream()
                .collect(Collectors.groupingBy(TreeDTO::getSpecies));

        List<Map<String, Object>> stats = new java.util.ArrayList<>();
        grouped.forEach((species, treeList) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("species", species);
            map.put("treeCount", treeList.size());
            map.put("totalVolume", treeList.stream().mapToDouble(TreeDTO::getVolume).sum());
            map.put("avgDbh", treeList.stream().mapToDouble(TreeDTO::getDbhAvg).average().orElse(0));
            map.put("avgHeight", treeList.stream().mapToDouble(TreeDTO::getTreeHeight).average().orElse(0));
            stats.add(map);
        });

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-trees")
    public ResponseEntity<List<TreeDTO>> getTopTrees(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(treeService.getTopTrees(limit));
    }

    /**
     * 导出单木数据 - 支持 CSV、Excel、JSON 格式
     */
    @GetMapping("/stand/export")
    public ResponseEntity<byte[]> exportTreesByStand(
            @RequestParam String standId,
            @RequestParam(defaultValue = "csv") String format) {

        log.info("导出单木数据, standId={}, format={}", standId, format);

        try {
            List<TreeDTO> trees;
            Integer standIdInt = null;

            try {
                standIdInt = Integer.parseInt(standId);
                trees = treeService.getTreesByStandId(standIdInt);
            } catch (NumberFormatException e) {
                log.warn("林分ID '{}' 不是数字格式，使用字符串查询", standId);
                trees = treeService.getTreesByStandIdString(standId);
                if (!trees.isEmpty() && trees.get(0).getStandId() != null) {
                    standIdInt = trees.get(0).getStandId();
                }
            }

            if (trees.isEmpty()) {
                log.warn("林分 {} 暂无单木数据", standId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("该小班暂无单木数据".getBytes(StandardCharsets.UTF_8));
            }

            byte[] data;
            String filename;
            String contentType;
            String fileId = standIdInt != null ? String.valueOf(standIdInt) : standId;

            switch (format.toLowerCase()) {
                case "excel", "xlsx" -> {
                    data = exportUtil.exportToExcel(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.xlsx", fileId);
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                }
                case "json" -> {
                    data = exportUtil.exportToJson(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.json", fileId);
                    contentType = "application/json;charset=UTF-8";
                }
                default -> {
                    data = exportUtil.exportToCsv(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.csv", fileId);
                    contentType = "text/csv;charset=UTF-8";
                }
            }

            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", encodedFilename);
            headers.setContentLength(data.length);

            headers.set("Access-Control-Expose-Headers", "Content-Disposition");

            log.info("导出成功, standId={}, format={}, 共 {} 条记录", standId, format, trees.size());

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("导出失败, standId={}, format={}: {}", standId, format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "tree-measurement-service");
        return ResponseEntity.ok(status);
    }
}