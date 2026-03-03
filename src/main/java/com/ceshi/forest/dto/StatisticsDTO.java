package com.ceshi.forest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor  // MyBatis 需要无参构造函数
@AllArgsConstructor
public class StatisticsDTO {
    private String species;
    private Long standCount;
    private Double totalArea;
    private Double totalVolume;
    private Double avgVolumePerHa;
}