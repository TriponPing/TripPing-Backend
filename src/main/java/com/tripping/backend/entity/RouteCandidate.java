package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_candidate")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RouteCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "condition_id", nullable = false)
    private Long conditionId;

    @Column(name = "candidate_order")
    private Integer candidateOrder; // 추천 1~5번째

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}