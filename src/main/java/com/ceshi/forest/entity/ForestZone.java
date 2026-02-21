package com.ceshi.forest.entity;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "forest_zone")
@EqualsAndHashCode(exclude = "stands")
@ToString(exclude = "stands")
public class ForestZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer zoneId;

    private String zoneName;
    private String zoneCode;
    private String adminRegion;
    private Double totalAreaHa;
    private Double forestAreaHa;
    private LocalDate establishedDate;
    private String managerName;
    private String contactPhone;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point geom;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ForestStand> stands = new ArrayList<>();
}