package com.tripping.backend.route.dto;

import com.tripping.backend.entity.PlannedRoute;
import lombok.Getter;

@Getter
public class RouteSearchResponse {
    private Long routeId;
    private String title;

    public RouteSearchResponse(PlannedRoute route) {
        this.routeId = route.getPlannedRouteId();
        this.title = route.getTitle();
    }
}