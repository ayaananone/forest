
package com.ceshi.forest.Controller;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.service.ForestStandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stands")
@CrossOrigin(origins = "*")  // 允许跨域，生产环境应配置具体域名
public class ForestStandController {

    @Autowired
    private ForestStandService standService;

    // 获取所有林分（用于地图展示）
    @GetMapping
    public ResponseEntity<List<StandDTO>> getAllStands() {
        return ResponseEntity.ok(standService.getAllStands());
    }

    // 根据ID获取林分详情
    @GetMapping("/{id}")
    public ResponseEntity<StandDTO> getStandById(@PathVariable Integer id) {
        return ResponseEntity.ok(standService.getStandById(id));
    }

    // 空间查询：获取指定半径内的林分
    @GetMapping("/nearby")
    public ResponseEntity<List<StandDTO>> getNearbyStands(
            @RequestParam Double lon,
            @RequestParam Double lat,
            @RequestParam(defaultValue = "45000") Integer radiusMeters) {
        return ResponseEntity.ok(standService.getNearbyStands(lon, lat, radiusMeters));
    }

    // 获取高价值林分
    @GetMapping("/high-value")
    public ResponseEntity<List<StandDTO>> getHighValueStands(
            @RequestParam(defaultValue = "120") Double minVolumePerHa) {
        return ResponseEntity.ok(standService.getHighValueStands(minVolumePerHa));
    }

    // 获取树种统计
    @GetMapping("/statistics/species")
    public ResponseEntity<List<StatisticsDTO>> getSpeciesStatistics() {
        return ResponseEntity.ok(standService.getSpeciesStatistics());
    }

    // 健康检查
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "forest-stand-service");
        return ResponseEntity.ok(status);
    }
}
