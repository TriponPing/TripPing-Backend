package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_product_spot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TourProductSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "stay_duration")
    private Integer stayDuration; // 분 단위
}