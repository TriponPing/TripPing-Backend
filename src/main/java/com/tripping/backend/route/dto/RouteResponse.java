package com.tripping.backend.route.dto;

import com.tripping.backend.entity.PlannedRoute;
import lombok.Getter;
import java.util.List;

@Getter
public class RouteResponse {
    private Long routeId;
    private String title;
    private Boolean isShared;
    private List<RoutePlaceResponse> places;

    public RouteResponse(PlannedRoute route, List<RoutePlaceResponse> places) {
        this.routeId = route.getPlannedRouteId();
        this.title = route.getTitle();
        this.isShared = route.getIsShared();
        this.places = places;
    }
}