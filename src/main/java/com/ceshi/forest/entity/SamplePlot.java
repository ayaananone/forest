package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SamplePlot {
    private Integer plotId;
    private Integer standId;
    private Integer plotNo;
    private Double plotAreaHa;
    private Double plotAreaM2;
    private LocalDate surveyDate;
    private Double longitudeDd;
    private Double latitudeDd;
    private Integer elevation;
    private Double slope;
    private String aspect;
    private Integer totalTrees;
    private Double avgDbh;
    private Double avgHeight;
    private Double canopyDensity;
    private Double totalVolume;
    private Double volumePerHa;
    private String surveyor;
    // geom 字段在查询时单独处理
}