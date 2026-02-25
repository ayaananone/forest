package com.ceshi.forest.repository;

import com.ceshi.forest.entity.TreeMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TreeMeasurementRepository extends JpaRepository<TreeMeasurement, Integer> {

    // 根据样地ID查询单木（通过关联对象查询）
    List<TreeMeasurement> findByPlotPlotId(Integer plotId);

    // 根据小班编号(stand_id)查询单木（通过关联对象查询）
    List<TreeMeasurement> findByStandStandId(Integer standId);

    // 查询大径级林木（胸径大于指定值）
    List<TreeMeasurement> findByDbhAvgGreaterThanEqual(Double minDbh);

    // 按树种统计
    @Query("SELECT t.species as species, " +
            "COUNT(t) as treeCount, " +
            "SUM(t.volume) as totalVolume, " +
            "AVG(t.dbhAvg) as avgDbh, " +
            "AVG(t.treeHeight) as avgHeight " +
            "FROM TreeMeasurement t GROUP BY t.species")
    List<Object[]> getStatisticsBySpecies();

    // 查询特定树种的单木
    List<TreeMeasurement> findBySpecies(String species);

    // 计算样地总蓄积（验证用）
    @Query("SELECT SUM(t.volume) FROM TreeMeasurement t WHERE t.plot.plotId = :plotId")
    Double calculatePlotVolume(@Param("plotId") Integer plotId);

}