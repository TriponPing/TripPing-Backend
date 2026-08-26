package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "planned_route_spot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlannedRouteSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "planned_route_id", nullable = false)
    private Long plannedRouteId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;
}