package com.ceshi.forest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tree_measurement")
@EqualsAndHashCode(exclude = {"plot", "stand"})
@ToString(exclude = {"plot", "stand"})
public class TreeMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer treeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plot_id")
    private SamplePlot plot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stand_id")
    private ForestStand stand;

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
    private LocalDate surveyDate;
}