package com.ceshi.forest.dto;

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

    // Getters and Setters
    public Integer getStandId() { return standId; }
    public void setStandId(Integer standId) { this.standId = standId; }
    public String getXiaoBanCode() { return xiaoBanCode; }
    public void setXiaoBanCode(String xiaoBanCode) { this.xiaoBanCode = xiaoBanCode; }
    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }
    public Double getAreaHa() { return areaHa; }
    public void setAreaHa(Double areaHa) { this.areaHa = areaHa; }
    public String getDominantSpecies() { return dominantSpecies; }
    public void setDominantSpecies(String dominantSpecies) { this.dominantSpecies = dominantSpecies; }
    public Integer getStandAge() { return standAge; }
    public void setStandAge(Integer standAge) { this.standAge = standAge; }
    public Double getVolumePerHa() { return volumePerHa; }
    public void setVolumePerHa(Double volumePerHa) { this.volumePerHa = volumePerHa; }
    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
    public Double getCanopyDensity() { return canopyDensity; }
    public void setCanopyDensity(Double canopyDensity) { this.canopyDensity = canopyDensity; }
    public Double getCenterLon() { return centerLon; }
    public void setCenterLon(Double centerLon) { this.centerLon = centerLon; }
    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
}
