package com.ceshi.forest.repository;

import com.ceshi.forest.entity.ForestStand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForestStandRepository extends JpaRepository<ForestStand, Integer> {

    // 根据小班代码查询
    Optional<ForestStand> findByXiaoBanCode(String xiaoBanCode);

    // 根据优势树种查询
    List<ForestStand> findByDominantSpecies(String dominantSpecies);

    // 查询高蓄积量林分
    List<ForestStand> findByVolumePerHaGreaterThanOrderByVolumePerHaDesc(Double minVolume);

    // 查询成熟林（年龄大于指定值）
    List<ForestStand> findByStandAgeGreaterThanEqual(Integer minAge);

    // 空间查询：查找指定半径内的林分（使用PostGIS函数）
    @Query(value = "SELECT * FROM forest_stand WHERE " +
            "ST_DWithin(geom::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusMeters) " +
            "ORDER BY ST_Distance(geom::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)",
            nativeQuery = true)
    List<ForestStand> findNearbyStands(@Param("lon") Double lon,
                                       @Param("lat") Double lat,
                                       @Param("radiusMeters") Integer radiusMeters);

    // 统计查询：按树种分组统计面积和蓄积
    @Query("SELECT s.dominantSpecies as species, " +
            "COUNT(s) as standCount, " +
            "SUM(s.areaHa) as totalArea, " +
            "SUM(s.totalVolume) as totalVolume, " +
            "AVG(s.volumePerHa) as avgVolumePerHa " +
            "FROM ForestStand s GROUP BY s.dominantSpecies")
    List<Object[]> getStatisticsBySpecies();

    // 范围查询：在矩形范围内的林分
    @Query(value = "SELECT * FROM forest_stand WHERE " +
            "center_lon BETWEEN :minLon AND :maxLon AND " +
            "center_lat BETWEEN :minLat AND :maxLat",
            nativeQuery = true)
    List<ForestStand> findInExtent(@Param("minLon") Double minLon,
                                   @Param("maxLon") Double maxLon,
                                   @Param("minLat") Double minLat,
                                   @Param("maxLat") Double maxLat);
}


