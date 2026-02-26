package com.ceshi.forest.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TreeMeasurement {
    private Integer treeId;
    private Integer plotId;
    private Integer standId;
    private Integer treeNo;
    private String species;
    private String speciesCode;
    private Double dbhDirection1;
    private Double dbhDirection2;
    private Double dbhAvg;
    private Double treeHeight;
    private Double diameterHalfHeight;
    private Double q2;
    private Double f1;
    private Double basalArea;
    private Double volume;
    private Double crownWidth;
    private String treeQuality;
    private String healthStatus;
    private LocalDate surveyDate;
}