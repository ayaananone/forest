package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandDTO {
    private Integer standId;
    private String xiaoBanCode;
    private String standName;
    private Double areaHa;
    private String dominantSpecies;
    private Integer standAge;
    private Double volumePerHa;
    private Double totalVolume;
    private Double canopyDensity;
    private Double centerLon;
    private Double centerLat;
    private String origin;
}