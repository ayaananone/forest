package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.StandDTO;
import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import com.ceshi.forest.mapper.ForestStandMapper;
import com.ceshi.forest.service.ForestStandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForestStandServiceImpl implements ForestStandService {

    private final ForestStandMapper standMapper;

    // ==================== 查询方法 ====================

    @Override
    public List<StandDTO> getAllStands() {
        return standMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StandDTO getStandById(Integer id) {
        ForestStand stand = standMapper.findById(id);
        if (stand == null) {
            throw new RuntimeException("林分不存在");
        }
        return convertToDTO(stand);
    }

    @Override
    public List<StandDTO> getNearbyStands(Double lon, Double lat, Integer radiusMeters) {
        return standMapper.findNearbyStands(lon, lat, radiusMeters).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StandDTO> getHighValueStands(Double minVolumePerHa) {
        return standMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsDTO> getSpeciesStatistics() {
        List<Map<String, Object>> results = standMapper.getStatisticsBySpecies();
        List<StatisticsDTO> stats = new ArrayList<>();

        for (Map<String, Object> row : results) {
            StatisticsDTO dto = new StatisticsDTO();
            dto.setSpecies((String) row.get("species"));
            dto.setStandCount(getLongValue(row.get("standCount")));
            dto.setTotalArea(getDoubleValue(row.get("totalArea")));
            dto.setTotalVolume(getDoubleValue(row.get("totalVolume")));
            dto.setAvgVolumePerHa(getDoubleValue(row.get("avgVolumePerHa")));
            stats.add(dto);
        }

        return stats;
    }

    // ==================== CRUD方法 ====================

    @Override
    @Transactional
    public StandDTO createStand(StandDTO dto, String operator) {
        // 检查编号重复
        if (dto.getXiaoBanCode() != null) {
            ForestStand exist = standMapper.findByXiaoBanCode(dto.getXiaoBanCode());
            if (exist != null) {
                throw new RuntimeException("林分编号已存在: " + dto.getXiaoBanCode());
            }
        }

        ForestStand entity = new ForestStand();
        BeanUtils.copyProperties(dto, entity);

        // 设置系统字段
        entity.setSurveyDate(dto.getSurveyDate() != null ? dto.getSurveyDate() : LocalDate.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        entity.setDeleted(false);

        // 计算总蓄积
        if (entity.getAreaHa() != null && entity.getVolumePerHa() != null) {
            entity.setTotalVolume(entity.getAreaHa() * entity.getVolumePerHa());
        }

        // 解析林班小班
        parseXiaoBanCode(entity);

        standMapper.insert(entity);

        log.info("创建林分成功: standId={}, xiaoBanCode={}", entity.getStandId(), entity.getXiaoBanCode());

        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public StandDTO updateStand(StandDTO dto, String operator) {
        ForestStand exist = standMapper.findById(dto.getStandId());
        if (exist == null) {
            throw new RuntimeException("林分不存在: " + dto.getStandId());
        }

        // 检查编号是否被其他记录使用
        if (!exist.getXiaoBanCode().equals(dto.getXiaoBanCode())) {
            ForestStand codeExist = standMapper.findByXiaoBanCode(dto.getXiaoBanCode());
            if (codeExist != null && !codeExist.getStandId().equals(dto.getStandId())) {
                throw new RuntimeException("林分编号已存在: " + dto.getXiaoBanCode());
            }
        }

        // 保留创建信息
        LocalDateTime createTime = exist.getCreateTime();
        String createBy = exist.getCreateBy();

        BeanUtils.copyProperties(dto, exist);

        exist.setCreateTime(createTime);
        exist.setCreateBy(createBy);
        exist.setUpdateTime(LocalDateTime.now());
        exist.setUpdateBy(operator);

        // 重新计算总蓄积
        if (exist.getAreaHa() != null && exist.getVolumePerHa() != null) {
            exist.setTotalVolume(exist.getAreaHa() * exist.getVolumePerHa());
        }

        parseXiaoBanCode(exist);

        standMapper.update(exist);

        log.info("更新林分成功: standId={}", exist.getStandId());

        return convertToDTO(exist);
    }

    @Override
    @Transactional
    public void deleteStand(Integer id, String operator) {
        ForestStand exist = standMapper.findById(id);
        if (exist == null) {
            throw new RuntimeException("林分不存在: " + id);
        }

        // 逻辑删除
        exist.setDeleted(true);
        exist.setUpdateTime(LocalDateTime.now());
        exist.setUpdateBy(operator);

        standMapper.update(exist);

        log.info("删除林分成功: standId={}", id);
    }

    @Override
    @Transactional
    public List<StandDTO> batchUpdateStands(List<StandDTO> dtoList, String operator) {
        return dtoList.stream()
                .map(dto -> updateStand(dto, operator))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void batchDeleteStands(List<Integer> ids, String operator) {
        ids.forEach(id -> deleteStand(id, operator));
    }

    @Override
    public boolean checkStandCodeExists(String xiaoBanCode, Integer excludeId) {
        ForestStand stand = standMapper.findByXiaoBanCode(xiaoBanCode);
        if (stand == null) {
            return false;
        }
        return excludeId == null || !excludeId.equals(stand.getStandId());
    }

    // ==================== 私有方法 ====================

    private void parseXiaoBanCode(ForestStand stand) {
        if (stand.getXiaoBanCode() != null && stand.getXiaoBanCode().contains("-")) {
            String[] parts = stand.getXiaoBanCode().split("-");
            if (parts.length == 2) {
                stand.setLinBan(parts[0]);
                stand.setXiaoBan(parts[1]);
            }
        }
    }

    /**
     * 转换为DTO - 简化处理，直接复制所有字段
     */
    private StandDTO convertToDTO(ForestStand stand) {
        StandDTO dto = new StandDTO();

        // 使用 BeanUtils 复制所有相同命名字段
        BeanUtils.copyProperties(stand, dto);

        // 确保 totalVolume 被正确设置（可能被覆盖，重新计算）
        if (dto.getTotalVolume() == null && dto.getAreaHa() != null && dto.getVolumePerHa() != null) {
            dto.setTotalVolume(dto.getAreaHa() * dto.getVolumePerHa());
        }

        return dto;
    }

    private Long getLongValue(Object obj) {
        return obj != null ? ((Number) obj).longValue() : 0L;
    }

    private Double getDoubleValue(Object obj) {
        return obj != null ? ((Number) obj).doubleValue() : 0.0;
    }
}