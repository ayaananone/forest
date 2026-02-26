package com.ceshi.forest.service.impl;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.entity.SamplePlot;
import com.ceshi.forest.mapper.SamplePlotMapper;
import com.ceshi.forest.mapper.TreeMeasurementMapper;
import com.ceshi.forest.service.SamplePlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样地服务实现类
 */
@Service
public class SamplePlotServiceImpl implements SamplePlotService {

    private static final Logger logger = LoggerFactory.getLogger(SamplePlotServiceImpl.class);

    @Autowired
    private SamplePlotMapper plotMapper;

    @Autowired
    private TreeMeasurementMapper treeMapper;

    @Override
    public List<PlotDTO> getAllPlots() {
        try {
            logger.info("获取所有样地数据");
            List<PlotDTO> result = plotMapper.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("成功获取 {} 条样地数据", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取所有样地数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取样地数据失败", e);
        }
    }

    @Override
    public PlotDTO getPlotById(Integer id) {
        try {
            logger.info("根据ID获取样地: {}", id);
            SamplePlot plot = plotMapper.findById(id);
            if (plot == null) {
                logger.warn("样地不存在: {}", id);
                throw new RuntimeException("样地不存在");
            }
            logger.info("成功获取样地: {}", plot.getPlotId());
            return convertToDTO(plot);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取样地详情失败, ID={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("获取样地详情失败", e);
        }
    }

    @Override
    public List<PlotDTO> getPlotsByStandId(Integer standId) {
        try {
            logger.info("获取林分下的样地列表, standId: {}", standId);
            List<PlotDTO> result = plotMapper.findByStandId(standId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("林分 {} 下找到 {} 个样地", standId, result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取林分样地列表失败, standId={}: {}", standId, e.getMessage(), e);
            throw new RuntimeException("获取林分样地列表失败", e);
        }
    }

    @Override
    public List<PlotDTO> getHighVolumePlots(Double minVolumePerHa) {
        try {
            logger.info("获取高蓄积样地, 最小蓄积: {} m³/ha", minVolumePerHa);
            List<PlotDTO> result = plotMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            logger.info("高蓄积样地查询成功, 找到 {} 个样地", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取高蓄积样地失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取高蓄积样地失败", e);
        }
    }

    @Override
    public Map<String, Object> getPlotStatistics(Integer standId) {
        try {
            logger.info("获取样地统计信息, standId: {}", standId);
            Object[] stats = plotMapper.getPlotStatisticsByStandId(standId);
            Map<String, Object> result = new HashMap<>();

            if (stats != null && stats.length > 0) {
                result.put("avgVolumePerHa", stats[0]);
                result.put("totalVolume", stats[1]);
                result.put("plotCount", stats[2]);
                logger.info("样地统计: 平均蓄积={}, 总蓄积={}, 样地数={}", stats[0], stats[1], stats[2]);
            } else {
                logger.warn("林分 {} 暂无样地统计数据", standId);
            }

            List<PlotDTO> plots = getPlotsByStandId(standId);
            result.put("plots", plots);

            return result;
        } catch (Exception e) {
            logger.error("获取样地统计失败, standId={}: {}", standId, e.getMessage(), e);
            throw new RuntimeException("获取样地统计失败", e);
        }
    }

    @Override
    public Map<String, Object> verifyPlotVolume(Integer plotId) {
        try {
            logger.info("验证样地蓄积, plotId: {}", plotId);
            SamplePlot plot = plotMapper.findById(plotId);
            if (plot == null) {
                logger.warn("样地不存在: {}", plotId);
                throw new RuntimeException("样地不存在");
            }

            Double calculatedVolume = treeMapper.calculatePlotVolume(plotId);
            if (calculatedVolume == null) calculatedVolume = 0.0;

            Double recordedVolume = plot.getTotalVolume();
            if (recordedVolume == null) recordedVolume = 0.0;

            Double difference = calculatedVolume - recordedVolume;
            Double percentDiff = recordedVolume > 0 ? (difference / recordedVolume) * 100 : 0;

            Map<String, Object> result = new HashMap<>();
            result.put("plotId", plotId);
            result.put("calculatedVolume", Math.round(calculatedVolume * 10000.0) / 10000.0);
            result.put("recordedVolume", recordedVolume);
            result.put("difference", Math.round(difference * 10000.0) / 10000.0);
            result.put("percentDifference", Math.round(percentDiff * 100.0) / 100.0);
            result.put("status", Math.abs(percentDiff) < 5 ? "正常" : "异常");

            logger.info("样地 {} 蓄积验证: 计算值={}, 记录值={}, 差异={}%, 状态={}",
                    plotId, calculatedVolume, recordedVolume, percentDiff, result.get("status"));

            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("验证样地蓄积失败, plotId={}: {}", plotId, e.getMessage(), e);
            throw new RuntimeException("验证样地蓄积失败", e);
        }
    }

    private PlotDTO convertToDTO(SamplePlot plot) {
        try {
            PlotDTO dto = new PlotDTO();
            dto.setPlotId(plot.getPlotId());
            dto.setStandId(plot.getStandId());
            dto.setStandName("");
            dto.setPlotNo(plot.getPlotNo());
            dto.setPlotAreaHa(plot.getPlotAreaHa());
            dto.setPlotAreaM2(plot.getPlotAreaM2());
            dto.setSurveyDate(plot.getSurveyDate() != null ? plot.getSurveyDate().toString() : null);
            dto.setLongitudeDd(plot.getLongitudeDd());
            dto.setLatitudeDd(plot.getLatitudeDd());
            dto.setElevation(plot.getElevation());
            dto.setSlope(plot.getSlope());
            dto.setAspect(plot.getAspect());
            dto.setTotalTrees(plot.getTotalTrees());
            dto.setAvgDbh(plot.getAvgDbh());
            dto.setAvgHeight(plot.getAvgHeight());
            dto.setCanopyDensity(plot.getCanopyDensity());
            dto.setTotalVolume(plot.getTotalVolume());
            dto.setVolumePerHa(plot.getVolumePerHa());
            dto.setSurveyor(plot.getSurveyor());
            return dto;
        } catch (Exception e) {
            logger.error("转换样地DTO失败, plotId={}: {}", plot.getPlotId(), e.getMessage());
            throw new RuntimeException("数据转换失败", e);
        }
    }
}