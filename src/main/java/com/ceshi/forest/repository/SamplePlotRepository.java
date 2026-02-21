package com.ceshi.forest.repository;

import com.ceshi.forest.entity.SamplePlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SamplePlotRepository extends JpaRepository<SamplePlot, Integer> {

    // 根据林分ID查询样地
    List<SamplePlot> findByStandStandId(Integer standId);

    // 查询高蓄积样地
    List<SamplePlot> findByVolumePerHaGreaterThan(Double minVolume);

    // 统计林分内的样地信息
    @Query("SELECT AVG(p.volumePerHa), SUM(p.totalVolume), COUNT(p) " +
            "FROM SamplePlot p WHERE p.stand.standId = :standId")
    Object[] getPlotStatisticsByStandId(@Param("standId") Integer standId);
}