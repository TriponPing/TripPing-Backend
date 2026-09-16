package com.tripping.backend.ping.dto;

import java.time.LocalDateTime;

/**
 * "다음 핑 찍기" 응답 - 계획된 방문 순서(visit_order)대로 큐처럼 다음 장소 하나를 확정함.
 * POST /routes/{routeId}/pings/next
 */
public record ConfirmNextPingResponse(
        Long actualRouteSpotId,
        Integer visitOrder,
        Long spotId,
        String spotName,
        LocalDateTime visitTime,
        boolean hasNext,       // 아직 안 찍은 다음 장소가 더 남아있는지
        String nextSpotName    // hasNext=true일 때만 값 있음 (그 다음에 찍을 장소 미리보기)
) {
}
