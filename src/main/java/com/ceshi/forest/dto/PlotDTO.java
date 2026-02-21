package com.ceshi.forest.dto;

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

    // Getters and Setters
    public Integer getPlotId() { return plotId; }
    public void setPlotId(Integer plotId) { this.plotId = plotId; }

    public Integer getStandId() { return standId; }
    public void setStandId(Integer standId) { this.standId = standId; }

    public String getStandName() { return standName; }
    public void setStandName(String standName) { this.standName = standName; }

    public Integer getPlotNo() { return plotNo; }
    public void setPlotNo(Integer plotNo) { this.plotNo = plotNo; }

    public Double getPlotAreaHa() { return plotAreaHa; }
    public void setPlotAreaHa(Double plotAreaHa) { this.plotAreaHa = plotAreaHa; }

    public Double getPlotAreaM2() { return plotAreaM2; }
    public void setPlotAreaM2(Double plotAreaM2) { this.plotAreaM2 = plotAreaM2; }

    public String getSurveyDate() { return surveyDate; }
    public void setSurveyDate(String surveyDate) { this.surveyDate = surveyDate; }

    public Double getLongitudeDd() { return longitudeDd; }
    public void setLongitudeDd(Double longitudeDd) { this.longitudeDd = longitudeDd; }

    public Double getLatitudeDd() { return latitudeDd; }
    public void setLatitudeDd(Double latitudeDd) { this.latitudeDd = latitudeDd; }

    public Integer getElevation() { return elevation; }
    public void setElevation(Integer elevation) { this.elevation = elevation; }

    public Double getSlope() { return slope; }
    public void setSlope(Double slope) { this.slope = slope; }

    public String getAspect() { return aspect; }
    public void setAspect(String aspect) { this.aspect = aspect; }

    public Integer getTotalTrees() { return totalTrees; }
    public void setTotalTrees(Integer totalTrees) { this.totalTrees = totalTrees; }

    public Double getAvgDbh() { return avgDbh; }
    public void setAvgDbh(Double avgDbh) { this.avgDbh = avgDbh; }

    public Double getAvgHeight() { return avgHeight; }
    public void setAvgHeight(Double avgHeight) { this.avgHeight = avgHeight; }

    public Double getCanopyDensity() { return canopyDensity; }
    public void setCanopyDensity(Double canopyDensity) { this.canopyDensity = canopyDensity; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Double getVolumePerHa() { return volumePerHa; }
    public void setVolumePerHa(Double volumePerHa) { this.volumePerHa = volumePerHa; }

    public String getSurveyor() { return surveyor; }
    public void setSurveyor(String surveyor) { this.surveyor = surveyor; }
}
