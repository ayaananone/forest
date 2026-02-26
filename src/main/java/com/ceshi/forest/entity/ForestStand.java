package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ForestStand {
    private Integer standId;
    private Integer zoneId;
    private String linBan;
    private String xiaoBan;
    private String xiaoBanCode;
    private String standName;
    private Double areaHa;
    private String siteType;
    private String origin;
    private Integer standAge;
    private Double canopyDensity;
    private Double avgHeight;
    private Double avgDbh;
    private Double volumePerHa;
    private Double totalVolume;
    private String dominantSpecies;
    private String speciesComposition;
    private Double centerLon;
    private Double centerLat;
    private Integer elevation;
    private Double slope;
    private String aspect;
    private LocalDate surveyDate;
    private String surveyor;
    // geom 字段在查询时单独处理
}