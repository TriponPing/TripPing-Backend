package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "actual_route_spot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ActualRouteSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "actual_route_spot_id")
    private Long actualRouteSpotId;

    @Column(name = "actual_route_id", nullable = false)
    private Long actualRouteId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "visit_time")
    private LocalDateTime visitTime;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude; // 핑 등록 시점 실제 GPS 위도

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude; // 핑 등록 시점 실제 GPS 경도
}