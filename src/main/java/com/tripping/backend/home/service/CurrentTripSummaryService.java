package com.tripping.backend.home.service;

import com.tripping.backend.entity.ActualRoute;
import com.tripping.backend.home.dto.response.CurrentTripSummaryResponse;
import com.tripping.backend.home.repository.HomeCurrentTripRepository;
import com.tripping.backend.trip.repository.TripActualRouteSpotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentTripSummaryService {

    private final HomeCurrentTripRepository currentTripRepository;
    // 👈 수정: WidgetPing(WIDGET_PING) 대신 Ping "기록" 탭이 보는 것과 같은
    // ACTUAL_ROUTE_SPOT을 보게 바꿈 - 두 화면이 서로 다른 테이블을 봐서 핑 개수/스테퍼가
    // 어긋나던 문제 수정 (정리 항목 1번-A).
    private final TripActualRouteSpotRepository actualRouteSpotRepository;

    public CurrentTripSummaryResponse getCurrentTripSummary(Long userId) {
        // 정상적으로는 유저당 IN_PROGRESS 여행이 1개여야 하지만, 테스트 중 완료 처리를 안 하고
        // 여러 번 시작해서 2개 이상 쌓인 경우도 있어 Pageable로 최신 1개만 안전하게 가져옴.
        List<ActualRoute> routes = currentTripRepository.findInProgressRoutes(userId, PageRequest.of(0, 1));
        if (routes.isEmpty()) {
            return null;
        }
        ActualRoute route = routes.get(0);

        List<TripActualRouteSpotRepository.TripRouteMapSpotProjection> spots =
                actualRouteSpotRepository.findMapSpotsByActualRouteId(route.getActualRouteId());
        long pingCount = spots.size();
        List<String> visitedPlaceNames = spots.stream()
                .map(TripActualRouteSpotRepository.TripRouteMapSpotProjection::getSpotName)
                .toList();

        // 👈 새로 추가: confirm(visitTime 등록)된 핑 개수 + 가장 최근에 찍힌 핑의 좌표.
        // spots는 visit_order ASC로 정렬되어 있고, confirmNextPing()이 항상 순서대로 앞에서부터
        // 확정시키므로 confirm된 핑들은 항상 리스트 앞쪽에 연속으로 몰려있음 -> 그 개수만 세면 됨.
        long confirmedCount = spots.stream()
                .filter(spot -> spot.getVisitTime() != null)
                .count();

        Double lastPingLatitude = null;
        Double lastPingLongitude = null;
        for (int i = spots.size() - 1; i >= 0; i--) {
            TripActualRouteSpotRepository.TripRouteMapSpotProjection spot = spots.get(i);
            if (spot.getVisitTime() != null) {
                lastPingLatitude = spot.getLatitude() != null ? spot.getLatitude().doubleValue() : null;
                lastPingLongitude = spot.getLongitude() != null ? spot.getLongitude().doubleValue() : null;
                break;
            }
        }

        return CurrentTripSummaryResponse.of(
                route, pingCount, visitedPlaceNames, confirmedCount, lastPingLatitude, lastPingLongitude);
    }
}
