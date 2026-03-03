package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandDTO {

    private Integer standId;

    @NotBlank(message = "林分编号不能为空")
    @Pattern(regexp = "\\d{2}-\\d{2}", message = "编号格式必须为：01-05")
    private String xiaoBanCode;

    @NotBlank(message = "林分名称不能为空")
    @Size(min = 2, max = 50, message = "名称长度2-50字符")
    private String standName;

    @NotNull(message = "面积不能为空")
    @DecimalMin(value = "0.01", message = "面积必须大于0")
    private Double areaHa;

    @NotBlank(message = "优势树种不能为空")
    private String dominantSpecies;

    // 树种组成 - 简化为JSON字符串，与数据库jsonb兼容
    private String speciesComposition;

    @Min(value = 1, message = "林龄必须大于0")
    @Max(value = 200, message = "林龄不能超过200年")
    private Integer standAge;

    @NotNull(message = "每公顷蓄积不能为空")
    @DecimalMin(value = "0", message = "蓄积不能为负数")
    private Double volumePerHa;

    private Double totalVolume;

    @DecimalMin(value = "0", message = "郁闭度不能为负数")
    @DecimalMax(value = "1", message = "郁闭度不能超过1")
    private Double canopyDensity;

    // 立地等级
    @Min(value = 1, message = "立地等级最小为1")
    @Max(value = 5, message = "立地等级最大为5")
    private Integer siteClass;

    // 坡向 - varchar
    private String aspect;

    // 平均胸径
    private Double avgDbh;

    // 平均树高
    private Double avgHeight;

    // 海拔 - int4
    private Integer elevation;

    // 立地类型
    private String siteType;

    // 坡度
    private Double slope;

    // 调查日期
    private LocalDate surveyDate;

    // 调查员
    private String surveyor;

    private Double centerLon;
    private Double centerLat;

    @NotBlank(message = "起源类型不能为空")
    private String origin;

    @Size(min = 0, max = 500, message = "备注不能超过500字")
    private String remark;
}