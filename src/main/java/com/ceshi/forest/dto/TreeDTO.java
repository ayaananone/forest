package com.ceshi.forest.dto;

public class TreeDTO {
    private Integer treeId;
    private Integer plotId;
    private Integer standId;
    private String standName;
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

    // Getters and Setters
    public Integer getTreeId() { return treeId; }
    public void setTreeId(Integer treeId) { this.treeId = treeId; }

    public Integer getPlotId() { return plotId; }
    public void setPlotId(Integer plotId) { this.plotId = plotId; }

    public Integer getStandId() { return standId; }
    public void setStandId(Integer standId) { this.standId = standId; }

    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }

    public Integer getTreeNo() { return treeNo; }
    public void setTreeNo(Integer treeNo) { this.treeNo = treeNo; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getSpeciesCode() { return speciesCode; }
    public void setSpeciesCode(String speciesCode) { this.speciesCode = speciesCode; }

    public Double getDbhDirection1() { return dbhDirection1; }
    public void setDbhDirection1(Double dbhDirection1) { this.dbhDirection1 = dbhDirection1; }

    public Double getDbhDirection2() { return dbhDirection2; }
    public void setDbhDirection2(Double dbhDirection2) { this.dbhDirection2 = dbhDirection2; }

    public Double getDbhAvg() { return dbhAvg; }
    public void setDbhAvg(Double dbhAvg) { this.dbhAvg = dbhAvg; }

    public Double getTreeHeight() { return treeHeight; }
    public void setTreeHeight(Double treeHeight) { this.treeHeight = treeHeight; }

    public Double getDiameterHalfHeight() { return diameterHalfHeight; }
    public void setDiameterHalfHeight(Double diameterHalfHeight) { this.diameterHalfHeight = diameterHalfHeight; }

    public Double getQ2() { return q2; }
    public void setQ2(Double q2) { this.q2 = q2; }

    public Double getF1() { return f1; }
    public void setF1(Double f1) { this.f1 = f1; }

    public Double getBasalArea() { return basalArea; }
    public void setBasalArea(Double basalArea) { this.basalArea = basalArea; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public Double getCrownWidth() { return crownWidth; }
    public void setCrownWidth(Double crownWidth) { this.crownWidth = crownWidth; }

    public String getTreeQuality() { return treeQuality; }
    public void setTreeQuality(String treeQuality) { this.treeQuality = treeQuality; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getSurveyDate() { return surveyDate; }
    public void setSurveyDate(String surveyDate) { this.surveyDate = surveyDate; }
}