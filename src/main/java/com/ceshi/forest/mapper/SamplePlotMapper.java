package com.ceshi.forest.mapper;

import com.ceshi.forest.entity.SamplePlot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SamplePlotMapper {

    List<SamplePlot> findAll();

    SamplePlot findById(Integer id);

    List<SamplePlot> findByStandId(Integer standId);

    List<SamplePlot> findByVolumePerHaGreaterThan(Double minVolume);

    Object[] getPlotStatisticsByStandId(Integer standId);
}