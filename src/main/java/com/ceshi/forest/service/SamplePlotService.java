package com.ceshi.forest.service;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.entity.SamplePlot;
import com.ceshi.forest.repository.SamplePlotRepository;
import com.ceshi.forest.repository.TreeMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SamplePlotService {

    @Autowired
    private SamplePlotRepository plotRepository;

    @Autowired
    private TreeMeasurementRepository treeRepository;

    // 获取所有样地
    public List<PlotDTO> getAllPlots() {
        return plotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 根据ID获取样地
    public PlotDTO getPlotById(Integer id) {
        SamplePlot plot = plotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("样地不存在"));
        return convertToDTO(plot);
    }

    // 根据林分ID获取样地列表
    public List<PlotDTO> getPlotsByStandId(Integer standId) {
        return plotRepository.findByStandStandId(standId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取高蓄积样地
    public List<PlotDTO> getHighVolumePlots(Double minVolumePerHa) {
        return plotRepository.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 获取样地统计信息
    public Map<String, Object> getPlotStatistics(Integer standId) {
        Object[] stats = plotRepository.getPlotStatisticsByStandId(standId);
        Map<String, Object> result = new HashMap<>();

        if (stats != null && stats.length > 0) {
            Object[] row = (Object[]) stats[0];
            result.put("avgVolumePerHa", row[0]);
            result.put("totalVolume", row[1]);
            result.put("plotCount", row[2]);
        }

        // 获取该林分的样地列表
        List<PlotDTO> plots = getPlotsByStandId(standId);
        result.put("plots", plots);

        return result;
    }

    // 验证样地蓄积（对比单木汇总和样地记录）
    public Map<String, Object> verifyPlotVolume(Integer plotId) {
        SamplePlot plot = plotRepository.findById(plotId)
                .orElseThrow(() -> new RuntimeException("样地不存在"));

        // 从单木表计算总蓄积
        Double calculatedVolume = treeRepository.calculatePlotVolume(plotId);
        if (calculatedVolume == null) calculatedVolume = 0.0;

        // 样地记录的蓄积
        Double recordedVolume = plot.getTotalVolume();
        if (recordedVolume == null) recordedVolume = 0.0;

        // 计算差异
        Double difference = calculatedVolume - recordedVolume;
        Double percentDiff = recordedVolume > 0 ?
                (difference / recordedVolume) * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("plotId", plotId);
        result.put("calculatedVolume", Math.round(calculatedVolume * 10000.0) / 10000.0);
        result.put("recordedVolume", recordedVolume);
        result.put("difference", Math.round(difference * 10000.0) / 10000.0);
        result.put("percentDifference", Math.round(percentDiff * 100.0) / 100.0);
        result.put("status", Math.abs(percentDiff) < 5 ? "正常" : "异常"); // 差异<5%认为正常

        return result;
    }

    // 转换为DTO
    private PlotDTO convertToDTO(SamplePlot plot) {
        PlotDTO dto = new PlotDTO();
        dto.setPlotId(plot.getPlotId());
        dto.setStandId(plot.getStand().getStandId());
        dto.setStandName(plot.getStand().getStandName());
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
    }
}
