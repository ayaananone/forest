package com.ceshi.forest.controller;

import com.ceshi.forest.dto.TreeDTO;
import com.ceshi.forest.service.TreeMeasurementService;
import com.ceshi.forest.util.ExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/trees")
@CrossOrigin(origins = "*")
public class TreeMeasurementController {

    private static final Logger logger = LoggerFactory.getLogger(TreeMeasurementController.class);

    @Autowired
    private TreeMeasurementService treeService;

    @Autowired
    private ExportUtil exportUtil;

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

    /**
     * 根据林分ID获取单木列表 - 支持数字和字符串类型的林分ID
     */
    @GetMapping("/stand/{standId}")
    public ResponseEntity<List<TreeDTO>> getTreesByStandId(@PathVariable String standId) {
        logger.info("收到请求: GET /api/trees/stand/{}", standId);
        try {
            List<TreeDTO> result;

            // 尝试作为数字ID查询
            try {
                Integer id = Integer.parseInt(standId);
                result = treeService.getTreesByStandId(id);
            } catch (NumberFormatException e) {
                // 不是数字，作为字符串编码查询
                logger.warn("林分ID '{}' 不是数字格式，尝试字符串查询", standId);
                result = treeService.getTreesByStandIdString(standId);
            }

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
    public ResponseEntity<List<Map<String, Object>>> getStandSpeciesStatistics(@PathVariable String standId) {
        logger.info("收到请求: GET /api/trees/stand/{}/statistics", standId);
        try {
            List<TreeDTO> trees;

            // 尝试作为数字ID查询
            try {
                Integer id = Integer.parseInt(standId);
                trees = treeService.getTreesByStandId(id);
            } catch (NumberFormatException e) {
                trees = treeService.getTreesByStandIdString(standId);
            }

            // 统计树种
            Map<String, List<TreeDTO>> grouped = trees.stream()
                    .collect(java.util.stream.Collectors.groupingBy(TreeDTO::getSpecies));

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

            logger.info("响应: GET /api/trees/stand/{}/statistics, 返回 {} 种树种", standId, stats.size());
            return ResponseEntity.ok(stats);
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

    /**
     * 导出单木数据 - 支持 CSV、Excel、JSON 格式
     * 兼容数字ID（如 123）和字符串编码（如 "02-05"）
     *
     * @param standId 林分ID（可以是数字或字符串，如 "123" 或 "02-05"）
     * @param format 导出格式：csv、excel、json（默认csv）
     * @return 导出的文件
     */
    @GetMapping("/stand/export")
    public ResponseEntity<byte[]> exportTreesByStand(
            @RequestParam String standId,
            @RequestParam(defaultValue = "csv") String format) {

        logger.info("收到请求: GET /api/trees/stand/export, standId={}, format={}", standId, format);

        try {
            List<TreeDTO> trees;
            Integer standIdInt = null;

            // 尝试作为数字ID查询
            try {
                standIdInt = Integer.parseInt(standId);
                trees = treeService.getTreesByStandId(standIdInt);
                logger.info("使用数字ID查询成功: {}", standIdInt);
            } catch (NumberFormatException e) {
                // 不是数字，作为字符串编码查询
                logger.warn("林分ID '{}' 不是数字格式，使用字符串查询", standId);
                trees = treeService.getTreesByStandIdString(standId);
                // 尝试从返回的数据中获取数字ID（用于文件名）
                if (!trees.isEmpty() && trees.get(0).getStandId() != null) {
                    standIdInt = trees.get(0).getStandId();
                }
            }

            if (trees.isEmpty()) {
                logger.warn("林分 {} 暂无单木数据", standId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("该小班暂无单木数据".getBytes(StandardCharsets.UTF_8));
            }

            byte[] data;
            String filename;
            String contentType;

            // 用于文件名的ID（优先使用数字ID）
            String fileId = standIdInt != null ? String.valueOf(standIdInt) : standId;

            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    data = exportUtil.exportToExcel(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.xlsx", fileId);
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;

                case "json":
                    data = exportUtil.exportToJson(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.json", fileId);
                    contentType = "application/json;charset=UTF-8";
                    break;

                case "csv":
                default:
                    data = exportUtil.exportToCsv(trees, standIdInt != null ? standIdInt : 0);
                    filename = String.format("小班_%s_单木数据.csv", fileId);
                    contentType = "text/csv;charset=UTF-8";
                    break;
            }

            // 对文件名进行编码，支持中文
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", encodedFilename);
            headers.setContentLength(data.length);
            // 添加 CORS 头
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            logger.info("响应: 导出林分 {} 单木数据成功，格式：{}，共 {} 条记录", standId, format, trees.size());

            return new ResponseEntity<>(data, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("导出单木数据失败, standId={}, format={}: {}", standId, format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
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