package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;

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

    private Double centerLon;
    private Double centerLat;

    @NotBlank(message = "起源类型不能为空")
    private String origin;
}