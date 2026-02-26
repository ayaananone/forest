package com.ceshi.forest.service;

import com.ceshi.forest.dto.PlotDTO;

import java.util.List;
import java.util.Map;

/**
 * 样地服务接口
 */
public interface SamplePlotService {

    /**
     * 获取所有样地
     */
    List<PlotDTO> getAllPlots();

    /**
     * 根据ID获取样地
     */
    PlotDTO getPlotById(Integer id);

    /**
     * 根据林分ID获取样地列表
     */
    List<PlotDTO> getPlotsByStandId(Integer standId);

    /**
     * 获取高蓄积样地
     */
    List<PlotDTO> getHighVolumePlots(Double minVolumePerHa);

    /**
     * 获取样地统计信息
     */
    Map<String, Object> getPlotStatistics(Integer standId);

    /**
     * 验证样地蓄积
     */
    Map<String, Object> verifyPlotVolume(Integer plotId);
}