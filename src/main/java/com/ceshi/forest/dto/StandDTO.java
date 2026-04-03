package com.ceshi.forest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StandDTO {
    private Integer standId;

    @NotBlank(message = "林分编号不能为空")
    @Pattern(regexp = "\\d{2}-\\d{2}", message = "格式：01-05")
    private String xiaoBanCode;

    @NotBlank(message = "林分名称不能为空")
    private String standName;

    @NotBlank(message = "优势树种不能为空")
    private String dominantSpecies;

    @NotBlank(message = "起源类型不能为空")
    private String origin;

    @NotNull(message = "面积不能为空")
    @DecimalMin("0.01")
    private Double areaHa;

    @NotNull(message = "每公顷蓄积不能为空")
    @DecimalMin("0")
    private Double volumePerHa;

    private Double totalVolume;
    private Integer standAge;

    @DecimalMin("0") @DecimalMax("1")
    private Double canopyDensity;

    private Double avgDbh;
    private Double avgHeight;
    private Integer elevation;
    private String aspect;
    private Double slope;
    private String siteType;
    private String speciesComposition;
    private LocalDate surveyDate;
    private String surveyor;
    private Double centerLon;
    private Double centerLat;
    private String remark;
    private Integer siteClass;
    private Integer zoneId;
}