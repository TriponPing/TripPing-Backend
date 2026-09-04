package com.tripping.backend.trip.dto;

import com.tripping.backend.entity.ActualRoute;
import lombok.Getter;

@Getter
public class CurrentTripResponse {
    private Long actualRouteId;
    private String travelDate;
    private String companionType;
    private String transport;
    private Integer memberCount;
    private String status;

    public CurrentTripResponse(ActualRoute route) {
        this.actualRouteId = route.getActualRouteId();
        // LocalDate -> String 변환을 위해 .toString() 추가
        this.travelDate = route.getTravelDate() != null ? route.getTravelDate().toString() : null;
        this.companionType = route.getCompanionType();
        this.transport = route.getTransport();
        this.memberCount = route.getMemberCount();
        this.status = route.getStatus() != null ? route.getStatus().name() : null;
    }
}