package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ForestStand {
    private Integer standId;

    private Integer zoneId;
    private String xiaoBanCode;
    private String linBan;
    private String xiaoBan;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private Boolean deleted;
    private String standName;
    private Double areaHa;
    private String dominantSpecies;
    private String speciesComposition;
    private Integer standAge;
    private Double volumePerHa;
    private Double totalVolume;
    private Double canopyDensity;
    private Integer siteClass;
    private String aspect;
    private Double avgDbh;
    private Double avgHeight;
    private Integer elevation;
    private String siteType;
    private Double slope;
    private LocalDate surveyDate;
    private String surveyor;
    private Double centerLon;
    private Double centerLat;
    private String origin;
    private String remark;
}
