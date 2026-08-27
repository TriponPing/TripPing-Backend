package com.tripping.backend.ping.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 진행 중 여행 조회
 * GET /routes/{routeId}/pings
 */
public record OngoingTripResponse(
        Long routeId,
        String status,
        LocalDate travelDate,
        List<PingResponse> pings
) {
}
