package com.tripping.backend.trip.dto;

import com.tripping.backend.entity.ActualRoute;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class TripResponse {
    private Long actualRouteId;
    private LocalDate travelDate;
    private String companionType;
    private String transport;
    private Integer memberCount;
    private String status;

    public TripResponse(ActualRoute actualRoute) {
        this.actualRouteId = actualRoute.getActualRouteId();
        this.travelDate = actualRoute.getTravelDate();
        this.companionType = actualRoute.getCompanionType();
        this.transport = actualRoute.getTransport();
        this.memberCount = actualRoute.getMemberCount();
        this.status = actualRoute.getStatus() != null ? actualRoute.getStatus().name() : null;
    }
}