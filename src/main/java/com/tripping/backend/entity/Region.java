package com.tripping.backend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Region {

    @Id
    @Column(name = "region_id", length = 3)
    private String regionId; // R0~R19 코드

    @Column(name = "region_name", length = 30, nullable = false)
    private String regionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_type", nullable = false)
    private RegionType regionType;
}