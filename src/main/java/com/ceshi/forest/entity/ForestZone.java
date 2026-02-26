package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ForestZone {
    private Integer zoneId;
    private String zoneName;
    private String zoneCode;
    private String adminRegion;
    private Double totalAreaHa;
    private Double forestAreaHa;
    private LocalDate establishedDate;
    private String managerName;
    private String contactPhone;
    // geom 字段在查询时单独处理
}