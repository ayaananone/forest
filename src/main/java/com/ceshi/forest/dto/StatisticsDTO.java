package com.ceshi.forest.dto;

public class StatisticsDTO {
    private String species;
    private Long standCount;
    private Double totalArea;
    private Double totalVolume;
    private Double avgVolumePerHa;

    // Getters and Setters
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public Long getStandCount() { return standCount; }
    public void setStandCount(Long standCount) { this.standCount = standCount; }
    public Double getTotalArea() { return totalArea; }
    public void setTotalArea(Double totalArea) { this.totalArea = totalArea; }
    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }
    public Double getAvgVolumePerHa() { return avgVolumePerHa; }
    public void setAvgVolumePerHa(Double avgVolumePerHa) { this.avgVolumePerHa = avgVolumePerHa; }
}