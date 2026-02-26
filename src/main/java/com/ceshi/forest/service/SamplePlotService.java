package com.ceshi.forest.service;

import com.ceshi.forest.dto.PlotDTO;
import com.ceshi.forest.entity.SamplePlot;
import com.ceshi.forest.mapper.SamplePlotMapper;
import com.ceshi.forest.mapper.TreeMeasurementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SamplePlotService {

    @Autowired
    private SamplePlotMapper plotMapper;

    @Autowired
    private TreeMeasurementMapper treeMapper;

    public List<PlotDTO> getAllPlots() {
        return plotMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PlotDTO getPlotById(Integer id) {
        SamplePlot plot = plotMapper.findById(id);
        if (plot == null) {
            throw new RuntimeException("样地不存在");
        }
        return convertToDTO(plot);
    }

    public List<PlotDTO> getPlotsByStandId(Integer standId) {
        return plotMapper.findByStandId(standId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PlotDTO> getHighVolumePlots(Double minVolumePerHa) {
        return plotMapper.findByVolumePerHaGreaterThan(minVolumePerHa).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPlotStatistics(Integer standId) {
        Object[] stats = plotMapper.getPlotStatisticsByStandId(standId);
        Map<String, Object> result = new HashMap<>();

        if (stats != null && stats.length > 0) {
            result.put("avgVolumePerHa", stats[0]);
            result.put("totalVolume", stats[1]);
            result.put("plotCount", stats[2]);
        }

        List<PlotDTO> plots = getPlotsByStandId(standId);
        result.put("plots", plots);

        return result;
    }

    public Map<String, Object> verifyPlotVolume(Integer plotId) {
        SamplePlot plot = plotMapper.findById(plotId);
        if (plot == null) {
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

        return result;
    }

    private PlotDTO convertToDTO(SamplePlot plot) {
        PlotDTO dto = new PlotDTO();
        dto.setPlotId(plot.getPlotId());
        dto.setStandId(plot.getStandId());
        // standName 需要额外查询，这里简化处理
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
    }
}