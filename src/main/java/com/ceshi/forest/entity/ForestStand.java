package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ForestStand {
    private Integer standId;
    private Integer zoneId;
    private String xiaoBanCode;
    private Double areaHa;
    private String dominantSpecies;
    private Object speciesComposition;  // jsonb 类型，用 Object 或 String
    private Double volumePerHa;
    private Double totalVolume;
    private Double centerLon;
    private Double centerLat;
    private String aspect;           // varchar
    private Double avgDbh;
    private Double avgHeight;
    private Double canopyDensity;
    private Integer elevation;       // int4
    private String linBan;
    private String origin;
    private String siteType;
    private Double slope;
    private Integer standAge;
    private String standName;
    private LocalDate surveyDate;
    private String surveyor;
    private String xiaoBan;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private Boolean deleted;
    private String remark;

    // 确保有 siteClass 字段
    private Integer siteClass;
}