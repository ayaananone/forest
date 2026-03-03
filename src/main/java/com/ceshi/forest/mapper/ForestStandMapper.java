package com.ceshi.forest.mapper;

import com.ceshi.forest.dto.StatisticsDTO;
import com.ceshi.forest.entity.ForestStand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ForestStandMapper {

    // ==================== 查询方法 ====================

    List<ForestStand> findAll();

    ForestStand findById(Integer id);

    ForestStand findByXiaoBanCode(String code);

    List<ForestStand> findByDominantSpecies(String species);

    List<ForestStand> findByVolumePerHaGreaterThan(Double minVolume);

    List<ForestStand> findByStandAgeGreaterThanEqual(Integer minAge);

    List<ForestStand> findNearbyStands(@Param("lon") Double lon, @Param("lat") Double lat, @Param("radius") Integer radius);

    List<StatisticsDTO> getStatisticsBySpecies();

    List<ForestStand> findInExtent(@Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
                                   @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);

    // ==================== CRUD方法 ====================

    int insert(ForestStand stand);

    int update(ForestStand stand);

    int deleteById(Integer id);

    int logicDeleteById(@Param("id") Integer id, @Param("updateBy") String updateBy);

    // ==================== 序列修复方法 ====================

    Integer selectMaxStandId();

    void resetSequence(@Param("nextVal") Integer nextVal);

    Integer getCurrentSequenceValue();

    default void autoFixSequence() {
        Integer maxId = selectMaxStandId();
        if (maxId != null && maxId > 0) {
            resetSequence(maxId + 1);
        }
    }
}