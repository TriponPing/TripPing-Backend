package com.tripping.backend.route.dto;

import com.tripping.backend.entity.PlannedRoute;
import lombok.Getter;
import java.util.List;

@Getter
public class RouteMapResponse {
    private Long routeId;
    private String title;
    private List<RouteMapPlaceResponse> places;

    public RouteMapResponse(PlannedRoute route, List<RouteMapPlaceResponse> places) {
        this.routeId = route.getPlannedRouteId();
        this.title = route.getTitle();
        this.places = places;
    }
}