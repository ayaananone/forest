package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlotDTO {
    private Integer plotId;
    private Integer standId;
    private String standName;
    private Integer plotNo;
    private Double plotAreaHa;
    private Double plotAreaM2;
    private String surveyDate;
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
}