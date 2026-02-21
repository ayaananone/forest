package com.ceshi.forest.entity;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "sample_plot")
@EqualsAndHashCode(exclude = "stand")
@ToString(exclude = "stand")
public class SamplePlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer plotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stand_id")
    private ForestStand stand;

    private Integer plotNo;
    private Double plotAreaHa;
    private Double plotAreaM2;
    private LocalDate surveyDate;
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

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;
}