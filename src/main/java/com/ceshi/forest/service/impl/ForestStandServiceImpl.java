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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForestStandServiceImpl implements ForestStandService {

    private final ForestStandMapper standMapper;

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
            throw new RuntimeException("林分不存在: " + id);
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
        return standMapper.getStatisticsBySpecies();
    }

    @Override
    @Transactional
    public StandDTO createStand(StandDTO dto, String operator) {
        ForestStand entity = new ForestStand();
        BeanUtils.copyProperties(dto, entity);

        entity.setStandId(null);
        entity.setSurveyDate(LocalDate.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        entity.setDeleted(false);

        if (entity.getAreaHa() != null && entity.getVolumePerHa() != null) {
            entity.setTotalVolume(entity.getAreaHa() * entity.getVolumePerHa());
        }

        standMapper.insert(entity);

        log.info("创建林分成功: id={}", entity.getStandId());

        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public StandDTO updateStand(StandDTO dto, String operator) {
        ForestStand exist = standMapper.findById(dto.getStandId());
        if (exist == null) {
            throw new RuntimeException("林分不存在: " + dto.getStandId());
        }

        LocalDateTime createTime = exist.getCreateTime();
        String createBy = exist.getCreateBy();

        BeanUtils.copyProperties(dto, exist);

        exist.setCreateTime(createTime);
        exist.setCreateBy(createBy);
        exist.setUpdateTime(LocalDateTime.now());
        exist.setUpdateBy(operator);

        if (exist.getAreaHa() != null && exist.getVolumePerHa() != null) {
            exist.setTotalVolume(exist.getAreaHa() * exist.getVolumePerHa());
        }

        standMapper.update(exist);

        log.info("更新林分成功: id={}", exist.getStandId());

        return convertToDTO(exist);
    }

    @Override
    @Transactional
    public void deleteStand(Integer id, String operator) {
        ForestStand exist = standMapper.findById(id);
        if (exist == null) {
            throw new RuntimeException("林分不存在: " + id);
        }

        exist.setDeleted(true);
        exist.setUpdateTime(LocalDateTime.now());
        exist.setUpdateBy(operator);

        standMapper.update(exist);

        log.info("删除林分成功: id={}", id);
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

    private StandDTO convertToDTO(ForestStand stand) {
        StandDTO dto = new StandDTO();

        // 基本信息
        dto.setStandId(stand.getStandId());
        dto.setStandName(stand.getStandName());
        dto.setXiaoBanCode(stand.getXiaoBanCode());
        dto.setAreaHa(stand.getAreaHa());
        dto.setZoneId(stand.getZoneId());

        // 树种信息
        dto.setDominantSpecies(stand.getDominantSpecies());
        dto.setSpeciesComposition(stand.getSpeciesComposition());

        // 蓄积信息
        dto.setVolumePerHa(stand.getVolumePerHa());
        dto.setTotalVolume(stand.getTotalVolume());

        // 林分特征
        dto.setStandAge(stand.getStandAge());
        dto.setCanopyDensity(stand.getCanopyDensity());
        dto.setAvgDbh(stand.getAvgDbh());
        dto.setAvgHeight(stand.getAvgHeight());

        // 地形因子
        dto.setElevation(stand.getElevation());
        dto.setSlope(stand.getSlope());
        dto.setAspect(stand.getAspect());
        dto.setSiteType(stand.getSiteType());

        // 位置信息
        dto.setCenterLon(stand.getCenterLon());
        dto.setCenterLat(stand.getCenterLat());

        // 调查信息
        dto.setSurveyDate(stand.getSurveyDate());
        dto.setSurveyor(stand.getSurveyor());

        // 其他
        dto.setOrigin(stand.getOrigin());
        dto.setSiteClass(stand.getSiteClass());
        dto.setRemark(stand.getRemark());

        // 调试日志
        if (dto.getXiaoBanCode() == null) {
            log.warn("转换 DTO 时 xiaoBanCode 为 null, standId={}", stand.getStandId());
        }

        return dto;
    }
}