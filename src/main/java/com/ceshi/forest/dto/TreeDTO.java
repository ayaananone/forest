package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeDTO {
    private Integer treeId;           // tree_id
    private Integer plotId;           // plot_id
    private Integer treeNo;           // tree_no
    private String species;           // species
    private Double dbhAvg;            // dbh_avg
    private Double treeHeight;        // tree_height
    private Double diameterHalfHeight; // diameter_half_height
    private Double q2;                // q2
    private Double f1;                // f1
    private Double basalArea;         // basal_area
    private Double volume;            // volume
    private Double crownWidth;        // crown_width
    private Double dbhDirection1;     // dbh_direction1
    private Double dbhDirection2;     // dbh_direction2
    private String healthStatus;      // health_status
    private String speciesCode;       // species_code
    private String surveyDate;        // survey_date
    private String treeQuality;       // tree_quality
    private Integer standId;          // stand_id
}