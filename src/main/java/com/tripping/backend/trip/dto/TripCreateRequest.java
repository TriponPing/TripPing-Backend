package com.tripping.backend.trip.dto;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class TripCreateRequest {
    private LocalDate travelDate;
    private String companionType;
    private String transport;
    private Integer memberCount;
}