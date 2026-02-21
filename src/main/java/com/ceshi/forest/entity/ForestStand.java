package com.ceshi.forest.entity;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "forest_stand")
@EqualsAndHashCode(exclude = "zone")
@ToString(exclude = "zone")
public class ForestStand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer standId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private ForestZone zone;

    private String linBan;
    private String xiaoBan;
    private String xiaoBanCode;
    private String standName;
    private Double areaHa;
    private String siteType;
    private String origin;
    private Integer standAge;
    private Double canopyDensity;
    private Double avgHeight;
    private Double avgDbh;
    private Double volumePerHa;
    private Double totalVolume;
    private String dominantSpecies;

    @Column(columnDefinition = "jsonb")
    private String speciesComposition;

    private Double centerLon;
    private Double centerLat;
    private Integer elevation;
    private Double slope;
    private String aspect;
    private LocalDate surveyDate;
    private String surveyor;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;
}