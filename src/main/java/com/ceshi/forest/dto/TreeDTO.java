package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeDTO {
    private Integer treeId;
    private Integer plotId;
    private Integer standId;      // 小班编号
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
    private String surveyDate;
}