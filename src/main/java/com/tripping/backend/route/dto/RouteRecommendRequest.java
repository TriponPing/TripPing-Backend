package com.tripping.backend.route.dto;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
public class RouteRecommendRequest {
    private String regionId;
    private Integer memberCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String companionType;
    private String ageGroup;
    private String transport;
    private Integer totalTime;
    private String startPlace;
    private String endPlace;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean mealIncluded;
    private Integer maxSpotCount;
    private Integer walkTimeLimit;
    private List<Long> mustVisitSpotIds;
    private List<Long> excludeSpotIds;
}