package com.tripping.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "route_condition")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RouteCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long conditionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "region_id", length = 3)
    private String regionId;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "companion_type", length = 30)
    private String companionType;

    @Column(name = "age_group", length = 20)
    private String ageGroup;

    @Column(length = 30)
    private String transport;

    @Column(name = "total_time")
    private Integer totalTime; // 분 단위

    @Column(name = "start_place", length = 100)
    private String startPlace;

    @Column(name = "end_place", length = 100)
    private String endPlace;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder.Default
    @Column(name = "meal_included")
    private Boolean mealIncluded = false;

    @Column(name = "max_spot_count")
    private Integer maxSpotCount;

    @Column(length = 50)
    private String theme;

    @Column(name = "walk_time_limit")
    private Integer walkTimeLimit; // 분 단위

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}