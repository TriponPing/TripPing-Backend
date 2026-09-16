package com.tripping.backend.ping.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Ping "기록" 탭에서 꾹 눌러 드래그로 순서를 바꾼 뒤, 새 순서 전체를 한 번에 저장하는 요청.
 * 이 여행(actualRouteId)에 속한 모든 actualRouteSpotId를 새 순서 그대로 담아 보내야 함.
 * PATCH /trips/{routeId}/spots/order
 */
public record ReorderTripSpotsRequest(
        @NotEmpty(message = "순서 목록은 비어 있을 수 없습니다.")
        List<Long> actualRouteSpotIds
) {
}
