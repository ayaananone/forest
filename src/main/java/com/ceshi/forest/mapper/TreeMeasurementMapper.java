package com.ceshi.forest.mapper;

import com.ceshi.forest.entity.TreeMeasurement;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TreeMeasurementMapper {

    List<TreeMeasurement> findAll();

    TreeMeasurement findById(Integer id);

    List<TreeMeasurement> findByPlotId(Integer plotId);

    List<TreeMeasurement> findByStandId(Integer standId);

    List<TreeMeasurement> findByDbhAvgGreaterThanEqual(Double minDbh);

    List<TreeMeasurement> findBySpecies(String species);

    List<Map<String, Object>> getStatisticsBySpecies();

    Double calculatePlotVolume(Integer plotId);
}