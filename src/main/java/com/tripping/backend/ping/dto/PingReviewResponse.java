package com.tripping.backend.ping.dto;

import java.time.LocalDateTime;

/**
 * Ping 후기 응답
 *
 * pingId는 PING_LOG.actual_route_spot_id 기준입니다.
 * PING_LOG 테이블이 actual_route_spot_id로만 연결되고 widget_ping_id 참조는 없어서,
 * 후기(리뷰)는 "확정된 방문 스팟(ACTUAL_ROUTE_SPOT)" 단위로 남긴다고 보고 구현했습니다.
 * WIDGET_PING → ACTUAL_ROUTE_SPOT 전환(여행 종료 시 확정) 로직은 이 도메인 밖(trip/route)에
 * 있다고 가정하니, 그 로직이 아직 없다면 이 API들은 404를 낼 수 있습니다 - 담당자와 확인 필요.
 */
public record PingReviewResponse(
        Long pingId,
        Integer rating,
        String photoUrl,
        String reviewComment,
        LocalDateTime updatedAt
) {
}
