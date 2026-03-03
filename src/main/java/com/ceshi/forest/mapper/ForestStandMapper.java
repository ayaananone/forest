package com.ceshi.forest.mapper;

import com.ceshi.forest.entity.ForestStand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForestStandMapper {

    // ==================== 查询方法 ====================

    /**
     * 查询所有林分（排除已删除）
     */
    List<ForestStand> findAll();

    /**
     * 根据ID查询林分
     */
    ForestStand findById(Integer id);

    /**
     * 根据小班编号查询林分
     */
    ForestStand findByXiaoBanCode(String code);

    /**
     * 根据优势树种查询林分
     */
    List<ForestStand> findByDominantSpecies(String species);

    /**
     * 查询每公顷蓄积量大于指定值的林分
     */
    List<ForestStand> findByVolumePerHaGreaterThan(Double minVolume);

    /**
     * 查询林龄大于等于指定值的林分
     */
    List<ForestStand> findByStandAgeGreaterThanEqual(Integer minAge);

    /**
     * 查询附近的林分（基于经纬度和半径）
     */
    List<ForestStand> findNearbyStands(@Param("lon") Double lon, @Param("lat") Double lat, @Param("radius") Integer radius);

    /**
     * 按优势树种统计
     */
    List<Map<String, Object>> getStatisticsBySpecies();

    /**
     * 范围查询林分
     */
    List<ForestStand> findInExtent(@Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
                                   @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);

    // ==================== CRUD方法 ====================

    /**
     * 插入新林分（使用数据库自动生成的主键）
     */
    int insert(ForestStand stand);

    /**
     * 更新林分信息
     */
    int update(ForestStand stand);

    /**
     * 物理删除林分
     */
    int deleteById(Integer id);

    /**
     * 逻辑删除林分
     */
    int logicDeleteById(@Param("id") Integer id, @Param("updateBy") String updateBy);

    // ==================== 序列修复方法（解决主键冲突问题） ====================

    /**
     * 查询当前表中最大的 stand_id
     */
    Integer selectMaxStandId();

    /**
     * 重置序列到指定值
     * @param nextVal 下一个序列值
     */
    void resetSequence(@Param("nextVal") Integer nextVal);

    /**
     * 查询当前序列值
     */
    Integer getCurrentSequenceValue();

    /**
     * 自动修复序列（将序列设置为最大ID+1）
     */
    default void autoFixSequence() {
        Integer maxId = selectMaxStandId();
        if (maxId != null && maxId > 0) {
            resetSequence(maxId + 1);
        }
    }
}
