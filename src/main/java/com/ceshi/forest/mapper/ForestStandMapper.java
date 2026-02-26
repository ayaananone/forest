package com.ceshi.forest.mapper;

import com.ceshi.forest.entity.ForestStand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForestStandMapper {

    List<ForestStand> findAll();

    ForestStand findById(Integer id);

    ForestStand findByXiaoBanCode(String code);

    List<ForestStand> findByDominantSpecies(String species);

    List<ForestStand> findByVolumePerHaGreaterThan(Double minVolume);

    List<ForestStand> findByStandAgeGreaterThanEqual(Integer minAge);

    List<ForestStand> findNearbyStands(@Param("lon") Double lon, @Param("lat") Double lat, @Param("radius") Integer radius);

    List<Map<String, Object>> getStatisticsBySpecies();

    List<ForestStand> findInExtent(@Param("minLon") Double minLon, @Param("maxLon") Double maxLon,
                                   @Param("minLat") Double minLat, @Param("maxLat") Double maxLat);
}