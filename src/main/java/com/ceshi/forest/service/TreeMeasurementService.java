package com.ceshi.forest.service;

import com.ceshi.forest.dto.TreeDTO;

import java.util.List;
import java.util.Map;

/**
 * 单木测量服务接口
 */
public interface TreeMeasurementService {

    /**
     * 获取所有单木
     */
    List<TreeDTO> getAllTrees();

    /**
     * 根据ID获取单木
     */
    TreeDTO getTreeById(Integer id);

    /**
     * 根据样地ID获取单木列表
     */
    List<TreeDTO> getTreesByPlotId(Integer plotId);

    /**
     * 根据林分ID获取单木列表
     */
    List<TreeDTO> getTreesByStandId(Integer standId);

    /**
     * 根据林分ID（字符串）获取单木列表
     * 用于支持非数字格式的林分ID（如 "02-05"）
     */
    List<TreeDTO> getTreesByStandIdString(String standId);

    /**
     * 获取大径级林木
     */
    List<TreeDTO> getLargeTrees(Double minDbh);

    /**
     * 按树种查询单木
     */
    List<TreeDTO> getTreesBySpecies(String species);

    /**
     * 获取全局树种统计
     */
    List<Map<String, Object>> getSpeciesStatistics();

    /**
     * 获取林分内的树种统计
     */
    List<Map<String, Object>> getStandSpeciesStatistics(Integer standId);

    /**
     * 获取Top N大树
     */
    List<TreeDTO> getTopTrees(Integer limit);
}