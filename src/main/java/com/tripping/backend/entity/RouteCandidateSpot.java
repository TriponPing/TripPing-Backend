package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_candidate_spot")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RouteCandidateSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;
}